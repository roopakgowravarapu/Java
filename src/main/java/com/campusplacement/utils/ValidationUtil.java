package com.campusplacement.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Utility class providing validation functions and password hashing.
 * Designed with simple, clear methods for ease of college viva explanation.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    /**
     * Checks if string is neither null nor empty whitespace.
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    /**
     * Validates standard email address format.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates 10-digit mobile phone number.
     */
    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates CGPA between 0.00 and 10.00.
     */
    public static boolean isValidCgpa(double cgpa) {
        return cgpa >= 0.0 && cgpa <= 10.0;
    }

    /**
     * Validates graduation year within reasonable academic range.
     */
    public static boolean isValidGraduationYear(int year) {
        return year >= 2000 && year <= 2050;
    }

    /**
     * Computes SHA-256 hash for secure database password storage.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }
}
