package com.campusplacement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Command-line Database Inspector Utility.
 * Displays all tables and rows in campus_placement.db.
 */
public class ViewDatabase {

    public static void main(String[] args) {
        String dbPath = "campus_placement.db";
        System.out.println("==================================================================================");
        System.out.println("   CAMPUS PLACEMENT SYSTEM - DATABASE VIEWER (campus_placement.db)");
        System.out.println("==================================================================================");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement stmt = conn.createStatement()) {

            printTable(stmt, "students");
            printTable(stmt, "users");
            printTable(stmt, "companies");
            printTable(stmt, "placement_drives");
            printTable(stmt, "applications");

        } catch (Exception e) {
            System.err.println("Database inspection error: " + e.getMessage());
        }

        System.out.println("==================================================================================");
    }

    private static void printTable(Statement stmt, String tableName) {
        System.out.println("\n>>> TABLE: " + tableName.toUpperCase());
        System.out.println("----------------------------------------------------------------------------------");
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Print header
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("%-20s", meta.getColumnName(i));
            }
            System.out.println();
            for (int i = 1; i <= colCount; i++) {
                System.out.print("--------------------");
            }
            System.out.println();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    if (val == null) val = "NULL";
                    if (val.length() > 18) val = val.substring(0, 15) + "...";
                    System.out.printf("%-20s", val);
                }
                System.out.println();
            }

            if (rowCount == 0) {
                System.out.println("(Table is currently empty)");
            }
            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("Total Rows: " + rowCount);
        } catch (Exception e) {
            System.out.println("Could not read table " + tableName + ": " + e.getMessage());
        }
    }
}
