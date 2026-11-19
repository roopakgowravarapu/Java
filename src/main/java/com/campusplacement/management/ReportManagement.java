package com.campusplacement.management;

import com.campusplacement.DBConnection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Handles statistical aggregations, SQL analytics, and report generation (CSV & PDF).
 * Real SQL queries with COUNT(), SUM(), AVG(), MAX(), MIN(), GROUP BY, and JOINS.
 */
public class ReportManagement {

    static {
        // Direct font cache to temp directory to avoid permission issues
        try {
            System.setProperty("pdfbox.fontcache", System.getProperty("java.io.tmpdir"));
        } catch (Exception ignored) {}
    }

    /**
     * Holds high-level dashboard KPIs.
     */
    public static class DashboardStats {
        public int totalStudents;
        public int totalCompanies;
        public int totalDrives;
        public int totalApplications;
        public int totalSelected;
        public int totalRejected;
        public double placementPercentage;
        public double averagePackage;
        public double highestPackage;
    }

    /**
     * Computes dashboard statistics from live database records.
     */
    public static DashboardStats getDashboardStatistics() throws SQLException {
        DashboardStats stats = new DashboardStats();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Total students
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next()) stats.totalStudents = rs.getInt(1);
            }

            // 2. Total companies
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM companies")) {
                if (rs.next()) stats.totalCompanies = rs.getInt(1);
            }

            // 3. Total drives
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM placement_drives")) {
                if (rs.next()) stats.totalDrives = rs.getInt(1);
            }

            // 4. Total applications
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM applications")) {
                if (rs.next()) stats.totalApplications = rs.getInt(1);
            }

            // 5. Selected applications
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM applications WHERE status = 'Selected'")) {
                if (rs.next()) stats.totalSelected = rs.getInt(1);
            }

            // 6. Rejected applications
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM applications WHERE status = 'Rejected'")) {
                if (rs.next()) stats.totalRejected = rs.getInt(1);
            }

            // 7. Placement Percentage
            if (stats.totalStudents > 0) {
                // Distinct students placed
                int placedStudents = stats.totalSelected;
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT student_id) FROM applications WHERE status = 'Selected'")) {
                    if (rs.next()) placedStudents = rs.getInt(1);
                }
                stats.placementPercentage = ((double) placedStudents / stats.totalStudents) * 100.0;
            }

            // 8. Highest and Average package of selected candidates
            String pkgSql = "SELECT COALESCE(AVG(d.package_lpa), 0.0) as avg_pkg, "
                          + "COALESCE(MAX(d.package_lpa), 0.0) as max_pkg "
                          + "FROM applications a "
                          + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                          + "WHERE a.status = 'Selected'";

            try (ResultSet rs = stmt.executeQuery(pkgSql)) {
                if (rs.next()) {
                    stats.averagePackage = rs.getDouble("avg_pkg");
                    stats.highestPackage = rs.getDouble("max_pkg");
                }
            }

            // If no selected candidate yet, fallback to available drives for metrics
            if (stats.highestPackage == 0.0) {
                try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(AVG(package_lpa), 0.0), COALESCE(MAX(package_lpa), 0.0) FROM placement_drives")) {
                    if (rs.next()) {
                        stats.averagePackage = rs.getDouble(1);
                        stats.highestPackage = rs.getDouble(2);
                    }
                }
            }
        }
        return stats;
    }

    /**
     * Chart 1: Department-wise student distribution.
     */
    public static Map<String, Integer> getDepartmentDistribution() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT department, COUNT(*) as count FROM students GROUP BY department ORDER BY count DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("department"), rs.getInt("count"));
            }
        }
        return map;
    }

    /**
     * Chart 2: Application status distribution.
     */
    public static Map<String, Integer> getApplicationStatusDistribution() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM applications GROUP BY status ORDER BY count DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("count"));
            }
        }
        return map;
    }

    /**
     * Chart 3: Company-wise placements.
     */
    public static Map<String, Integer> getCompanyPlacements() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.company_name, COUNT(a.application_id) as placed_count "
                   + "FROM applications a "
                   + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "WHERE a.status = 'Selected' "
                   + "GROUP BY c.company_name "
                   + "ORDER BY placed_count DESC LIMIT 6";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("company_name"), rs.getInt("placed_count"));
            }
        }
        return map;
    }

    /**
     * Chart 4: Package comparison of top drives.
     */
    public static Map<String, Double> getPackageComparison() throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT c.company_name || ' - ' || d.job_role as label, d.package_lpa "
                   + "FROM placement_drives d "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "ORDER BY d.package_lpa DESC LIMIT 5";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("label"), rs.getDouble("package_lpa"));
            }
        }
        return map;
    }

    /**
     * Exports tabular report data to a standard CSV file.
     */
    public static void exportToCSV(File targetFile, String[] headers, List<String[]> rows) throws IOException {
        try (FileWriter writer = new FileWriter(targetFile)) {
            // Write headers
            for (int i = 0; i < headers.length; i++) {
                writer.append(escapeCsv(headers[i]));
                if (i < headers.length - 1) writer.append(",");
            }
            writer.append("\n");

            // Write rows
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    writer.append(escapeCsv(row[i]));
                    if (i < row.length - 1) writer.append(",");
                }
                writer.append("\n");
            }
            writer.flush();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Exports tabular report data to a clean, formatted PDF file using Apache PDFBox.
     */
    public static void exportToPDF(File targetFile, String title, String[] headers, List<String[]> rows) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float margin = 40;
                float yStart = 780;
                float pageWidth = page.getMediaBox().getWidth();

                // Title
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(margin, yStart);
                cs.showText("CAMPUS PLACEMENT MANAGEMENT SYSTEM");
                cs.endText();

                // Subtitle
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, yStart - 20);
                cs.showText("Report: " + title);
                cs.endText();

                // Timestamp
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                cs.newLineAtOffset(margin, yStart - 35);
                cs.showText("Generated On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cs.endText();

                // Divider line
                cs.setLineWidth(1.0f);
                cs.moveTo(margin, yStart - 45);
                cs.lineTo(pageWidth - margin, yStart - 45);
                cs.stroke();

                // Table Layout
                float y = yStart - 65;
                float colWidth = (pageWidth - (margin * 2)) / headers.length;

                // Headers
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                for (int i = 0; i < headers.length; i++) {
                    cs.beginText();
                    cs.newLineAtOffset(margin + (i * colWidth), y);
                    cs.showText(truncateString(headers[i], 18));
                    cs.endText();
                }

                // Header underline
                y -= 8;
                cs.moveTo(margin, y);
                cs.lineTo(pageWidth - margin, y);
                cs.stroke();

                // Table Rows
                cs.setFont(PDType1Font.HELVETICA, 8);
                int count = 0;
                for (String[] row : rows) {
                    y -= 15;
                    if (y < 50) {
                        break; // fit in page (for student demo presentation)
                    }
                    for (int i = 0; i < row.length && i < headers.length; i++) {
                        cs.beginText();
                        cs.newLineAtOffset(margin + (i * colWidth), y);
                        String val = row[i] != null ? row[i] : "";
                        cs.showText(truncateString(val, 20));
                        cs.endText();
                    }
                    count++;
                }

                // Footer
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                cs.newLineAtOffset(margin, 30);
                cs.showText("Total Records Exported: " + count + " | Campus Placement System Official Report");
                cs.endText();
            }

            document.save(targetFile);
        }
    }

    private static String truncateString(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 2) + "..";
    }
}
