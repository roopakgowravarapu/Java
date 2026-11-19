package com.campusplacement.management;

import com.campusplacement.DBConnection;
import com.campusplacement.model.PlacementApplication;
import com.campusplacement.model.PlacementDrive;
import com.campusplacement.model.Student;
import com.campusplacement.utils.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Placement Applications and Status Workflow.
 * Uses database transactions for atomic status changes, audit history, and notifications.
 */
public class ApplicationManagement {

    /**
     * Represents an entry in the application history audit trail.
     */
    public static class HistoryEntry {
        private final int historyId;
        private final String oldStatus;
        private final String newStatus;
        private final String updatedBy;
        private final String comments;
        private final String changedAt;

        public HistoryEntry(int historyId, String oldStatus, String newStatus, 
                            String updatedBy, String comments, String changedAt) {
            this.historyId = historyId;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.updatedBy = updatedBy;
            this.comments = comments;
            this.changedAt = changedAt;
        }

        public int getHistoryId() { return historyId; }
        public String getOldStatus() { return oldStatus; }
        public String getNewStatus() { return newStatus; }
        public String getUpdatedBy() { return updatedBy; }
        public String getComments() { return comments; }
        public String getChangedAt() { return changedAt; }
    }

    /**
     * Submits a new placement application.
     * Enforces eligibility check, duplicate check, and transaction logging.
     */
    public static boolean applyForDrive(int driveId, int studentId, String remarks) throws SQLException {
        Student student = StudentManagement.getStudentById(studentId);
        PlacementDrive drive = DriveManagement.getDriveById(driveId);

        if (student == null || drive == null) {
            throw new IllegalArgumentException("Student or Drive record does not exist.");
        }

        // Eligibility validation
        DriveManagement.EligibilityResult eligibility = DriveManagement.checkEligibility(student, drive);
        if (!eligibility.isEligible()) {
            throw new IllegalStateException("Student is not eligible: " + eligibility.getSummary());
        }

        // Duplicate check
        if (hasAlreadyApplied(driveId, studentId)) {
            throw new IllegalStateException("You have already applied for this placement drive.");
        }

        String insertAppSql = "INSERT INTO applications (drive_id, student_id, status, remarks) VALUES (?, ?, 'Applied', ?)";
        String insertHistSql = "INSERT INTO application_history (application_id, old_status, new_status, updated_by, comments) VALUES (?, NULL, 'Applied', ?, ?)";
        String insertNotifSql = "INSERT INTO notifications (user_id, title, message, notification_type) VALUES (?, ?, ?, 'STATUS_CHANGE')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Begin Transaction
            try {
                int newAppId = 0;
                try (PreparedStatement stmt = conn.prepareStatement(insertAppSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, driveId);
                    stmt.setInt(2, studentId);
                    stmt.setString(3, remarks != null ? remarks : "Applied online");
                    stmt.executeUpdate();
                    try (ResultSet gk = stmt.getGeneratedKeys()) {
                        if (gk.next()) {
                            newAppId = gk.getInt(1);
                        }
                    }
                }

                // Insert into History
                try (PreparedStatement stmt = conn.prepareStatement(insertHistSql)) {
                    stmt.setInt(1, newAppId);
                    stmt.setString(2, student.getFullName());
                    stmt.setString(3, "Initial application submission");
                    stmt.executeUpdate();
                }

                // Notify Student User
                Integer userId = getUserIdForStudent(conn, studentId);
                if (userId != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertNotifSql)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, "Application Submitted: " + drive.getCompanyName());
                        stmt.setString(3, "Your application for " + drive.getJobRole() + " at " + drive.getCompanyName() + " has been successfully submitted.");
                        stmt.executeUpdate();
                    }
                }

                conn.commit(); // Commit Transaction
                return true;
            } catch (Exception ex) {
                conn.rollback(); // Rollback on error
                throw new SQLException("Failed to process application: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Checks if a student has already applied for a specific drive.
     */
    public static boolean hasAlreadyApplied(int driveId, int studentId) throws SQLException {
        String sql = "SELECT application_id FROM applications WHERE drive_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driveId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Updates an application status (Shortlisted, Interview Scheduled, Selected, Rejected).
     * Uses atomic transaction: updates applications, inserts into history, and sends notification.
     */
    public static boolean updateApplicationStatus(int applicationId, String newStatus, 
                                                  String interviewDate, String comments, 
                                                  String updatedBy) throws SQLException {
        String queryCurrent = "SELECT a.status, a.student_id, d.job_role, c.company_name "
                            + "FROM applications a "
                            + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                            + "JOIN companies c ON d.company_id = c.company_id "
                            + "WHERE a.application_id = ?";

        String updateSql = "UPDATE applications SET status = ?, interview_date = ?, remarks = ? WHERE application_id = ?";
        String historySql = "INSERT INTO application_history (application_id, old_status, new_status, updated_by, comments) VALUES (?, ?, ?, ?, ?)";
        String notifSql = "INSERT INTO notifications (user_id, title, message, notification_type) VALUES (?, ?, ?, 'STATUS_CHANGE')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String oldStatus = "Applied";
                int studentId = 0;
                String jobRole = "";
                String companyName = "";

                try (PreparedStatement stmt = conn.prepareStatement(queryCurrent)) {
                    stmt.setInt(1, applicationId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            oldStatus = rs.getString("status");
                            studentId = rs.getInt("student_id");
                            jobRole = rs.getString("job_role");
                            companyName = rs.getString("company_name");
                        } else {
                            throw new IllegalArgumentException("Application not found.");
                        }
                    }
                }

                // 1. Update application
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, newStatus);
                    stmt.setString(2, interviewDate != null && !interviewDate.trim().isEmpty() ? interviewDate.trim() : null);
                    stmt.setString(3, comments != null ? comments.trim() : "");
                    stmt.setInt(4, applicationId);
                    stmt.executeUpdate();
                }

                // 2. Insert History Audit
                try (PreparedStatement stmt = conn.prepareStatement(historySql)) {
                    stmt.setInt(1, applicationId);
                    stmt.setString(2, oldStatus);
                    stmt.setString(3, newStatus);
                    stmt.setString(4, updatedBy != null ? updatedBy : "Officer");
                    stmt.setString(5, comments != null ? comments.trim() : "Status updated to " + newStatus);
                    stmt.executeUpdate();
                }

                // 3. Send Notification to Student
                Integer userId = getUserIdForStudent(conn, studentId);
                if (userId != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(notifSql)) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, "Application Update: " + companyName);
                        String msg = "Your application for " + jobRole + " at " + companyName 
                                   + " has been updated to: " + newStatus.toUpperCase();
                        if ("Interview Scheduled".equalsIgnoreCase(newStatus) && interviewDate != null) {
                            msg += " (Interview Time: " + interviewDate + ")";
                        }
                        stmt.setString(3, msg);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw new SQLException("Failed to update application status: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves all applications with full join details.
     */
    public static List<PlacementApplication> getAllApplications() throws SQLException {
        List<PlacementApplication> list = new ArrayList<>();
        String sql = "SELECT a.application_id, a.drive_id, c.company_name, d.job_role, d.package_lpa, "
                   + "a.student_id, s.roll_number, s.full_name, s.department, s.cgpa, "
                   + "a.application_date, a.status, a.interview_date, a.remarks "
                   + "FROM applications a "
                   + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "JOIN students s ON a.student_id = s.student_id "
                   + "ORDER BY a.application_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToApplication(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves applications for a specific student.
     */
    public static List<PlacementApplication> getApplicationsByStudentId(int studentId) throws SQLException {
        List<PlacementApplication> list = new ArrayList<>();
        String sql = "SELECT a.application_id, a.drive_id, c.company_name, d.job_role, d.package_lpa, "
                   + "a.student_id, s.roll_number, s.full_name, s.department, s.cgpa, "
                   + "a.application_date, a.status, a.interview_date, a.remarks "
                   + "FROM applications a "
                   + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                   + "JOIN companies c ON d.company_id = c.company_id "
                   + "JOIN students s ON a.student_id = s.student_id "
                   + "WHERE a.student_id = ? "
                   + "ORDER BY a.application_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves audit history for an application.
     */
    public static List<HistoryEntry> getApplicationHistory(int applicationId) throws SQLException {
        List<HistoryEntry> list = new ArrayList<>();
        String sql = "SELECT history_id, old_status, new_status, updated_by, comments, changed_at "
                   + "FROM application_history WHERE application_id = ? ORDER BY history_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, applicationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new HistoryEntry(
                            rs.getInt("history_id"),
                            rs.getString("old_status"),
                            rs.getString("new_status"),
                            rs.getString("updated_by"),
                            rs.getString("comments"),
                            rs.getString("changed_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Filters applications by keyword and status.
     */
    public static List<PlacementApplication> searchApplications(String keyword, String status) throws SQLException {
        List<PlacementApplication> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.application_id, a.drive_id, c.company_name, d.job_role, d.package_lpa, "
                + "a.student_id, s.roll_number, s.full_name, s.department, s.cgpa, "
                + "a.application_date, a.status, a.interview_date, a.remarks "
                + "FROM applications a "
                + "JOIN placement_drives d ON a.drive_id = d.drive_id "
                + "JOIN companies c ON d.company_id = c.company_id "
                + "JOIN students s ON a.student_id = s.student_id "
                + "WHERE 1=1 ");

        boolean hasKeyword = ValidationUtil.isNotEmpty(keyword);
        boolean hasStatus = ValidationUtil.isNotEmpty(status) && !status.equalsIgnoreCase("All Statuses");

        if (hasKeyword) {
            sql.append("AND (LOWER(s.full_name) LIKE ? OR LOWER(s.roll_number) LIKE ? OR LOWER(c.company_name) LIKE ? OR LOWER(d.job_role) LIKE ?) ");
        }
        if (hasStatus) {
            sql.append("AND a.status = ? ");
        }
        sql.append("ORDER BY a.application_id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasKeyword) {
                String pat = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(idx++, pat);
                stmt.setString(idx++, pat);
                stmt.setString(idx++, pat);
                stmt.setString(idx++, pat);
            }
            if (hasStatus) {
                stmt.setString(idx++, status.trim());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToApplication(rs));
                }
            }
        }
        return list;
    }

    private static Integer getUserIdForStudent(Connection conn, int studentId) {
        String sql = "SELECT user_id FROM users WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static PlacementApplication mapResultSetToApplication(ResultSet rs) throws SQLException {
        return new PlacementApplication(
                rs.getInt("application_id"),
                rs.getInt("drive_id"),
                rs.getString("company_name"),
                rs.getString("job_role"),
                rs.getDouble("package_lpa"),
                rs.getInt("student_id"),
                rs.getString("roll_number"),
                rs.getString("full_name"),
                rs.getString("department"),
                rs.getDouble("cgpa"),
                rs.getString("application_date"),
                rs.getString("status"),
                rs.getString("interview_date"),
                rs.getString("remarks")
        );
    }

    // ==========================================================
    // NOTIFICATION DATABASE OPERATIONS
    // ==========================================================

    /**
     * Retrieves notifications for the active user (including broadcasts where user_id is null).
     */
    public static List<com.campusplacement.model.Notification> getNotificationsForUser(Integer userId) throws SQLException {
        List<com.campusplacement.model.Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, title, message, notification_type, is_read, created_at "
                   + "FROM notifications "
                   + "WHERE user_id IS NULL " + (userId != null ? "OR user_id = ? " : "")
                   + "ORDER BY notification_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int uidVal = rs.getInt("user_id");
                    Integer uId = rs.wasNull() ? null : uidVal;
                    list.add(new com.campusplacement.model.Notification(
                            rs.getInt("notification_id"),
                            uId,
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getString("notification_type"),
                            rs.getInt("is_read") == 1,
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Marks a notification as read.
     */
    public static boolean markNotificationAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Gets unread notification count for the active user.
     */
    public static int getUnreadNotificationCount(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE is_read = 0 "
                   + "AND (user_id IS NULL " + (userId != null ? "OR user_id = ?" : "") + ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (userId != null) {
                stmt.setInt(1, userId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
