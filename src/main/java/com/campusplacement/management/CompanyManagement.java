package com.campusplacement.management;

import com.campusplacement.DBConnection;
import com.campusplacement.model.Company;
import com.campusplacement.utils.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database operations for Recruiting Companies using direct JDBC.
 */
public class CompanyManagement {

    /**
     * Retrieves all companies sorted alphabetically by company name.
     */
    public static List<Company> getAllCompanies() throws SQLException {
        List<Company> list = new ArrayList<>();
        String sql = "SELECT company_id, company_name, industry, location, website, hr_contact, email "
                   + "FROM companies ORDER BY company_name ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCompany(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves a single company by ID.
     */
    public static Company getCompanyById(int companyId) throws SQLException {
        String sql = "SELECT company_id, company_name, industry, location, website, hr_contact, email "
                   + "FROM companies WHERE company_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompany(rs);
                }
            }
        }
        return null;
    }

    /**
     * Adds a new company record.
     */
    public static boolean addCompany(Company company) throws SQLException {
        validateCompany(company, 0);

        String sql = "INSERT INTO companies (company_name, industry, location, website, hr_contact, email) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, company.getCompanyName().trim());
            stmt.setString(2, company.getIndustry().trim());
            stmt.setString(3, company.getLocation().trim());
            stmt.setString(4, company.getWebsite() != null ? company.getWebsite().trim() : "");
            stmt.setString(5, company.getHrContact().trim());
            stmt.setString(6, company.getEmail().trim());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        company.setCompanyId(gk.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing company record.
     */
    public static boolean updateCompany(Company company) throws SQLException {
        validateCompany(company, company.getCompanyId());

        String sql = "UPDATE companies SET company_name = ?, industry = ?, location = ?, "
                   + "website = ?, hr_contact = ?, email = ? WHERE company_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company.getCompanyName().trim());
            stmt.setString(2, company.getIndustry().trim());
            stmt.setString(3, company.getLocation().trim());
            stmt.setString(4, company.getWebsite() != null ? company.getWebsite().trim() : "");
            stmt.setString(5, company.getHrContact().trim());
            stmt.setString(6, company.getEmail().trim());
            stmt.setInt(7, company.getCompanyId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a company record by ID.
     */
    public static boolean deleteCompany(int companyId) throws SQLException {
        String sql = "DELETE FROM companies WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, companyId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Filters companies by search keyword and industry.
     */
    public static List<Company> searchCompanies(String keyword, String industry) throws SQLException {
        List<Company> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT company_id, company_name, industry, location, website, hr_contact, email "
                + "FROM companies WHERE 1=1 ");

        boolean hasKeyword = ValidationUtil.isNotEmpty(keyword);
        boolean hasInd = ValidationUtil.isNotEmpty(industry) && !industry.equalsIgnoreCase("All Industries");

        if (hasKeyword) {
            sql.append("AND (LOWER(company_name) LIKE ? OR LOWER(location) LIKE ? OR LOWER(hr_contact) LIKE ?) ");
        }
        if (hasInd) {
            sql.append("AND industry = ? ");
        }
        sql.append("ORDER BY company_name ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (hasKeyword) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
                stmt.setString(index++, pattern);
            }
            if (hasInd) {
                stmt.setString(index++, industry.trim());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCompany(rs));
                }
            }
        }
        return list;
    }

    private static void validateCompany(Company c, int currentCompanyId) throws SQLException {
        if (!ValidationUtil.isNotEmpty(c.getCompanyName())) {
            throw new IllegalArgumentException("Company Name is required.");
        }
        if (!ValidationUtil.isNotEmpty(c.getIndustry())) {
            throw new IllegalArgumentException("Industry is required.");
        }
        if (!ValidationUtil.isNotEmpty(c.getLocation())) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (!ValidationUtil.isValidEmail(c.getEmail())) {
            throw new IllegalArgumentException("A valid Contact Email is required.");
        }
        if (!ValidationUtil.isNotEmpty(c.getHrContact())) {
            throw new IllegalArgumentException("HR Contact Person Name is required.");
        }

        // Duplicate name check
        String sql = "SELECT company_id FROM companies WHERE LOWER(company_name) = ? AND company_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, c.getCompanyName().trim().toLowerCase());
            stmt.setInt(2, currentCompanyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Company '" + c.getCompanyName() + "' already exists.");
                }
            }
        }
    }

    private static Company mapResultSetToCompany(ResultSet rs) throws SQLException {
        return new Company(
                rs.getInt("company_id"),
                rs.getString("company_name"),
                rs.getString("industry"),
                rs.getString("location"),
                rs.getString("website"),
                rs.getString("hr_contact"),
                rs.getString("email")
        );
    }
}
