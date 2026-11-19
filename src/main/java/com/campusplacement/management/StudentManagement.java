package com.campusplacement.management;

import com.campusplacement.DBConnection;
import com.campusplacement.model.Student;
import com.campusplacement.utils.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for Student records using direct JDBC.
 */
public class StudentManagement {

    /**
     * Retrieves all students sorted by student_id descending.
     */
    public static List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, roll_number, full_name, email, phone_number, "
                   + "department, cgpa, graduation_year, skills, backlog_count "
                   + "FROM students ORDER BY student_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves a single student by their ID.
     */
    public static Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, roll_number, full_name, email, phone_number, "
                   + "department, cgpa, graduation_year, skills, backlog_count "
                   + "FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new student into the database with validation.
     */
    public static boolean addStudent(Student student) throws SQLException {
        validateStudent(student, 0);

        String sql = "INSERT INTO students (roll_number, full_name, email, phone_number, "
                   + "department, cgpa, graduation_year, skills, backlog_count) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getRollNumber().trim().toUpperCase());
            stmt.setString(2, student.getFullName().trim());
            stmt.setString(3, student.getEmail().trim());
            stmt.setString(4, student.getPhoneNumber().trim());
            stmt.setString(5, student.getDepartment().trim());
            stmt.setDouble(6, student.getCgpa());
            stmt.setInt(7, student.getGraduationYear());
            stmt.setString(8, student.getSkills().trim());
            stmt.setInt(9, student.getBacklogCount());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing student record in the database.
     */
    public static boolean updateStudent(Student student) throws SQLException {
        validateStudent(student, student.getStudentId());

        String sql = "UPDATE students SET roll_number = ?, full_name = ?, email = ?, "
                   + "phone_number = ?, department = ?, cgpa = ?, graduation_year = ?, "
                   + "skills = ?, backlog_count = ? WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getRollNumber().trim().toUpperCase());
            stmt.setString(2, student.getFullName().trim());
            stmt.setString(3, student.getEmail().trim());
            stmt.setString(4, student.getPhoneNumber().trim());
            stmt.setString(5, student.getDepartment().trim());
            stmt.setDouble(6, student.getCgpa());
            stmt.setInt(7, student.getGraduationYear());
            stmt.setString(8, student.getSkills().trim());
            stmt.setInt(9, student.getBacklogCount());
            stmt.setInt(10, student.getStudentId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student record by ID.
     */
    public static boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Dynamically searches students by keyword (name, roll, skills) and filters by department.
     */
    public static List<Student> searchStudents(String keyword, String department) throws SQLException {
        List<Student> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT student_id, roll_number, full_name, email, phone_number, "
                + "department, cgpa, graduation_year, skills, backlog_count FROM students WHERE 1=1 ");

        boolean hasKeyword = ValidationUtil.isNotEmpty(keyword);
        boolean hasDept = ValidationUtil.isNotEmpty(department) && !department.equalsIgnoreCase("All Departments");

        if (hasKeyword) {
            sql.append("AND (LOWER(full_name) LIKE ? OR LOWER(roll_number) LIKE ? OR LOWER(skills) LIKE ?) ");
        }
        if (hasDept) {
            sql.append("AND department = ? ");
        }
        sql.append("ORDER BY cgpa DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (hasKeyword) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
                stmt.setString(paramIndex++, pattern);
            }
            if (hasDept) {
                stmt.setString(paramIndex++, department.trim());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs));
                }
            }
        }
        return list;
    }

    /**
     * Validates student data integrity before database persistence.
     */
    private static void validateStudent(Student s, int currentStudentId) throws SQLException {
        if (!ValidationUtil.isNotEmpty(s.getRollNumber())) {
            throw new IllegalArgumentException("Roll Number is required.");
        }
        if (!ValidationUtil.isNotEmpty(s.getFullName())) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (!ValidationUtil.isValidEmail(s.getEmail())) {
            throw new IllegalArgumentException("A valid Email address is required.");
        }
        if (!ValidationUtil.isValidPhone(s.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone Number must be a valid 10-digit number.");
        }
        if (!ValidationUtil.isValidCgpa(s.getCgpa())) {
            throw new IllegalArgumentException("CGPA must be between 0.00 and 10.00.");
        }
        if (s.getBacklogCount() < 0) {
            throw new IllegalArgumentException("Backlog count cannot be negative.");
        }
        if (!ValidationUtil.isValidGraduationYear(s.getGraduationYear())) {
            throw new IllegalArgumentException("Graduation year must be between 2000 and 2050.");
        }

        // Check for duplicate Roll Number
        String rollSql = "SELECT student_id FROM students WHERE UPPER(roll_number) = ? AND student_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(rollSql)) {
            stmt.setString(1, s.getRollNumber().trim().toUpperCase());
            stmt.setInt(2, currentStudentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Roll Number '" + s.getRollNumber() + "' is already registered.");
                }
            }
        }

        // Check for duplicate Email
        String emailSql = "SELECT student_id FROM students WHERE LOWER(email) = ? AND student_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(emailSql)) {
            stmt.setString(1, s.getEmail().trim().toLowerCase());
            stmt.setInt(2, currentStudentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Email '" + s.getEmail() + "' is already registered to another student.");
                }
            }
        }
    }

    private static Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("roll_number"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("department"),
                rs.getDouble("cgpa"),
                rs.getInt("graduation_year"),
                rs.getString("skills"),
                rs.getInt("backlog_count")
        );
    }
}
