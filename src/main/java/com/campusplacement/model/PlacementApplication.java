package com.campusplacement.model;

/**
 * Model class representing a Student's Application for a Placement Drive.
 */
public class PlacementApplication {
    private int applicationId;
    private int driveId;
    private String companyName;
    private String jobRole;
    private double packageLpa;
    private int studentId;
    private String studentRollNumber;
    private String studentName;
    private String studentDepartment;
    private double studentCgpa;
    private String applicationDate;
    private String status; // 'Applied', 'Under Review', 'Shortlisted', 'Interview Scheduled', 'Selected', 'Rejected'
    private String interviewDate;
    private String remarks;

    public PlacementApplication() {
    }

    public PlacementApplication(int applicationId, int driveId, String companyName, 
                                String jobRole, double packageLpa, int studentId, 
                                String studentRollNumber, String studentName, 
                                String studentDepartment, double studentCgpa, 
                                String applicationDate, String status, 
                                String interviewDate, String remarks) {
        this.applicationId = applicationId;
        this.driveId = driveId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.packageLpa = packageLpa;
        this.studentId = studentId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.studentDepartment = studentDepartment;
        this.studentCgpa = studentCgpa;
        this.applicationDate = applicationDate;
        this.status = status;
        this.interviewDate = interviewDate;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getDriveId() {
        return driveId;
    }

    public void setDriveId(int driveId) {
        this.driveId = driveId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public double getStudentCgpa() {
        return studentCgpa;
    }

    public void setStudentCgpa(double studentCgpa) {
        this.studentCgpa = studentCgpa;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return studentName + " (" + studentRollNumber + ") - " + companyName + " [" + status + "]";
    }
}
