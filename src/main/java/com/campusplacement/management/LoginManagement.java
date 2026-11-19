package com.campusplacement.management;

import com.campusplacement.DBConnection;
import com.campusplacement.utils.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles database-backed user authentication and role management.
 * Direct JDBC implementation without complex frameworks or DAO layers.
 */
public class LoginManagement {

    /**
     * Represents the currently authenticated user session.
     */
    public static class UserSession {
        private final int userId;
        private final String username;
        private final String role; // 'ADMIN', 'OFFICER', 'STUDENT'
        private final String email;
        private final Integer studentId;

        public UserSession(int userId, String username, String role, String email, Integer studentId) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.email = email;
            this.studentId = studentId;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public Integer getStudentId() { return studentId; }

        public boolean isOfficer() { return "OFFICER".equalsIgnoreCase(role); }
        public boolean isStudent() { return "STUDENT".equalsIgnoreCase(role); }
    }

    private static UserSession currentUser = null;

    /**
     * Authenticates a user by username or email and plain text password.
     * Uses PreparedStatement to prevent SQL Injection and validates SHA-256 hash.
     */
    public static UserSession authenticate(String usernameOrEmail, String password) throws SQLException {
        if (!ValidationUtil.isNotEmpty(usernameOrEmail) || !ValidationUtil.isNotEmpty(password)) {
            throw new IllegalArgumentException("Username/Email and Password cannot be empty.");
        }

        String passwordHash = ValidationUtil.hashPassword(password);

        String sql = "SELECT user_id, username, password_hash, role, email, student_id "
                   + "FROM users "
                   + "WHERE (LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameOrEmail.trim());
            stmt.setString(2, usernameOrEmail.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (storedHash.equalsIgnoreCase(passwordHash)) {
                        int userId = rs.getInt("user_id");
                        String username = rs.getString("username");
                        String role = rs.getString("role");
                        String email = rs.getString("email");
                        int stIdVal = rs.getInt("student_id");
                        Integer studentId = rs.wasNull() ? null : stIdVal;

                        currentUser = new UserSession(userId, username, role, email, studentId);
                        return currentUser;
                    }
                }
            }
        }
        return null; // Authentication failed
    }

    public static UserSession getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserSession session) {
        currentUser = session;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
