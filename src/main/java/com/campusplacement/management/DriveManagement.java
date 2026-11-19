package com.campusplacement.management;

import com.campusplacement.DBConnection;
import com.campusplacement.model.PlacementDrive;
import com.campusplacement.model.Student;
import com.campusplacement.utils.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles database operations and eligibility calculations for Placement Drives.
 */
public class DriveManagement {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Eligibility verification result.
     */
    public static class EligibilityResult {
        private final boolean eligible;
        private final List<String> reasons;

        public EligibilityResult(boolean eligible, List<String> reasons) {
            this.eligible = eligible;
            this.reasons = reasons;
        }

        public boolean isEligible() { return eligible; }
        public List<String> getReasons() { return reasons; }

        public String getSummary() {
            if (eligible) {
                return "Eligible to apply";
            }
            return String.join("; ", reasons);
        }
    }

    /**
     * Retrieves all placement drives with joined company name.
     */
    public static List<PlacementDrive> getAllDrives() throws SQLException {
        List<PlacementDrive> list = new ArrayList<>();
        String sql = "SELECT d.drive_id, d.company_id, c.company_name, d.job_role, d.job_description, "
                   + "d.package_lpa, d.min_cgpa, d.eligible_departments, d.max_backlogs, d.required_skills, "
                   + "d.application_deadline, d.drive_status "
                   + "FROM placement_drives d "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "ORDER BY d.drive_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToDrive(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves a single drive by ID with joined company details.
     */
    public static PlacementDrive getDriveById(int driveId) throws SQLException {
        String sql = "SELECT d.drive_id, d.company_id, c.company_name, d.job_role, d.job_description, "
                   + "d.package_lpa, d.min_cgpa, d.eligible_departments, d.max_backlogs, d.required_skills, "
                   + "d.application_deadline, d.drive_status "
                   + "FROM placement_drives d "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "WHERE d.drive_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driveId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDrive(rs);
                }
            }
        }
        return null;
    }

    /**
     * Creates a new placement drive.
     */
    public static boolean addDrive(PlacementDrive drive) throws SQLException {
        validateDrive(drive);

        String sql = "INSERT INTO placement_drives (company_id, job_role, job_description, "
                   + "package_lpa, min_cgpa, eligible_departments, max_backlogs, "
                   + "required_skills, application_deadline, drive_status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, drive.getCompanyId());
            stmt.setString(2, drive.getJobRole().trim());
            stmt.setString(3, drive.getJobDescription() != null ? drive.getJobDescription().trim() : "");
            stmt.setDouble(4, drive.getPackageLpa());
            stmt.setDouble(5, drive.getMinCgpa());
            stmt.setString(6, drive.getEligibleDepartments().trim());
            stmt.setInt(7, drive.getMaxBacklogs());
            stmt.setString(8, drive.getRequiredSkills().trim());
            stmt.setString(9, drive.getApplicationDeadline().trim());
            stmt.setString(10, drive.getDriveStatus() != null ? drive.getDriveStatus() : "OPEN");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        drive.setDriveId(gk.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing placement drive.
     */
    public static boolean updateDrive(PlacementDrive drive) throws SQLException {
        validateDrive(drive);

        String sql = "UPDATE placement_drives SET company_id = ?, job_role = ?, job_description = ?, "
                   + "package_lpa = ?, min_cgpa = ?, eligible_departments = ?, max_backlogs = ?, "
                   + "required_skills = ?, application_deadline = ?, drive_status = ? "
                   + "WHERE drive_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, drive.getCompanyId());
            stmt.setString(2, drive.getJobRole().trim());
            stmt.setString(3, drive.getJobDescription() != null ? drive.getJobDescription().trim() : "");
            stmt.setDouble(4, drive.getPackageLpa());
            stmt.setDouble(5, drive.getMinCgpa());
            stmt.setString(6, drive.getEligibleDepartments().trim());
            stmt.setInt(7, drive.getMaxBacklogs());
            stmt.setString(8, drive.getRequiredSkills().trim());
            stmt.setString(9, drive.getApplicationDeadline().trim());
            stmt.setString(10, drive.getDriveStatus());
            stmt.setInt(11, drive.getDriveId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a drive by ID and cascades through its applications.
     */
    public static boolean deleteDrive(int driveId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete application history for applications of this drive
                try (PreparedStatement ahStmt = conn.prepareStatement(
                        "DELETE FROM application_history WHERE application_id IN (SELECT application_id FROM applications WHERE drive_id = ?)")) {
                    ahStmt.setInt(1, driveId);
                    ahStmt.executeUpdate();
                }

                // 2. Delete applications for this drive
                try (PreparedStatement aStmt = conn.prepareStatement("DELETE FROM applications WHERE drive_id = ?")) {
                    aStmt.setInt(1, driveId);
                    aStmt.executeUpdate();
                }

                // 3. Delete the drive
                int affected;
                try (PreparedStatement dStmt = conn.prepareStatement("DELETE FROM placement_drives WHERE drive_id = ?")) {
                    dStmt.setInt(1, driveId);
                    affected = dStmt.executeUpdate();
                }

                conn.commit();
                return affected > 0;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Evaluates whether a student is eligible for a specific placement drive.
     * Checks CGPA, department, backlogs, and deadline.
     */
    public static EligibilityResult checkEligibility(Student student, PlacementDrive drive) {
        List<String> reasons = new ArrayList<>();

        if (student == null || drive == null) {
            reasons.add("Invalid student or drive record.");
            return new EligibilityResult(false, reasons);
        }

        // 1. CGPA check
        if (student.getCgpa() < drive.getMinCgpa()) {
            reasons.add(String.format("CGPA %.2f is below required minimum %.2f", student.getCgpa(), drive.getMinCgpa()));
        }

        // 2. Department check
        String[] depts = drive.getEligibleDepartments().split(",");
        boolean deptMatch = false;
        for (String d : depts) {
            if (d.trim().equalsIgnoreCase(student.getDepartment().trim())) {
                deptMatch = true;
                break;
            }
        }
        if (!deptMatch) {
            reasons.add("Department '" + student.getDepartment() + "' is not eligible for this drive (" + drive.getEligibleDepartments() + ")");
        }

        // 3. Backlog check
        if (student.getBacklogCount() > drive.getMaxBacklogs()) {
            reasons.add(String.format("Backlogs (%d) exceed maximum allowed (%d)", student.getBacklogCount(), drive.getMaxBacklogs()));
        }

        // 4. Drive Status & Deadline check
        if (!"OPEN".equalsIgnoreCase(drive.getDriveStatus())) {
            reasons.add("Drive is currently " + drive.getDriveStatus());
        }

        try {
            LocalDateTime deadline = LocalDateTime.parse(drive.getApplicationDeadline().replace(" ", "T"));
            if (LocalDateTime.now().isAfter(deadline)) {
                reasons.add("Application deadline passed on " + drive.getApplicationDeadline());
            }
        } catch (Exception e) {
            // If date format differs, allow
        }

        boolean isEligible = reasons.isEmpty();
        return new EligibilityResult(isEligible, reasons);
    }

    /**
     * Filters drives dynamically.
     */
    public static List<PlacementDrive> searchDrives(String keyword, Integer companyId, Double minPackage) throws SQLException {
        List<PlacementDrive> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.drive_id, d.company_id, c.company_name, d.job_role, d.job_description, "
                + "d.package_lpa, d.min_cgpa, d.eligible_departments, d.max_backlogs, d.required_skills, "
                + "d.application_deadline, d.drive_status "
                + "FROM placement_drives d "
                + "JOIN companies c ON d.company_id = c.company_id "
                + "WHERE 1=1 ");

        boolean hasKeyword = ValidationUtil.isNotEmpty(keyword);
        boolean hasCompany = companyId != null && companyId > 0;
        boolean hasPackage = minPackage != null && minPackage > 0;

        if (hasKeyword) {
            sql.append("AND (LOWER(c.company_name) LIKE ? OR LOWER(d.job_role) LIKE ? OR LOWER(d.required_skills) LIKE ?) ");
        }
        if (hasCompany) {
            sql.append("AND d.company_id = ? ");
        }
        if (hasPackage) {
            sql.append("AND d.package_lpa >= ? ");
        }
        sql.append("ORDER BY d.package_lpa DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasKeyword) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
                stmt.setString(idx++, pattern);
            }
            if (hasCompany) {
                stmt.setInt(idx++, companyId);
            }
            if (hasPackage) {
                stmt.setDouble(idx++, minPackage);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDrive(rs));
                }
            }
        }
        return list;
    }

    private static void validateDrive(PlacementDrive d) {
        if (d.getCompanyId() <= 0) {
            throw new IllegalArgumentException("A valid recruiting company must be selected.");
        }
        if (!ValidationUtil.isNotEmpty(d.getJobRole())) {
            throw new IllegalArgumentException("Job Role is required.");
        }
        if (d.getPackageLpa() <= 0) {
            throw new IllegalArgumentException("Package (LPA) must be greater than 0.");
        }
        if (!ValidationUtil.isValidCgpa(d.getMinCgpa())) {
            throw new IllegalArgumentException("Minimum CGPA must be between 0.00 and 10.00.");
        }
        if (!ValidationUtil.isNotEmpty(d.getEligibleDepartments())) {
            throw new IllegalArgumentException("At least one eligible department must be specified.");
        }
        if (d.getMaxBacklogs() < 0) {
            throw new IllegalArgumentException("Maximum backlogs cannot be negative.");
        }
        if (!ValidationUtil.isNotEmpty(d.getApplicationDeadline())) {
            throw new IllegalArgumentException("Application deadline is required.");
        }
    }

    private static PlacementDrive mapResultSetToDrive(ResultSet rs) throws SQLException {
        return new PlacementDrive(
                rs.getInt("drive_id"),
                rs.getInt("company_id"),
                rs.getString("company_name"),
                rs.getString("job_role"),
                rs.getString("job_description"),
                rs.getDouble("package_lpa"),
                rs.getDouble("min_cgpa"),
                rs.getString("eligible_departments"),
                rs.getInt("max_backlogs"),
                rs.getString("required_skills"),
                rs.getString("application_deadline"),
                rs.getString("drive_status")
        );
    }
}
