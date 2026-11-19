package com.campusplacement.model;

/**
 * Model class representing a Student in the Campus Placement System.
 * Contains student academic details, skills, and placement criteria.
 */
public class Student {
    private int studentId;
    private String rollNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String department;
    private double cgpa;
    private int graduationYear;
    private String skills;
    private int backlogCount;

    public Student() {
    }

    public Student(int studentId, String rollNumber, String fullName, String email, 
                   String phoneNumber, String department, double cgpa, 
                   int graduationYear, String skills, int backlogCount) {
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
        this.skills = skills;
        this.backlogCount = backlogCount;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getBacklogCount() {
        return backlogCount;
    }

    public void setBacklogCount(int backlogCount) {
        this.backlogCount = backlogCount;
    }

    @Override
    public String toString() {
        return rollNumber + " - " + fullName + " (" + department + ", CGPA: " + cgpa + ")";
    }
}
