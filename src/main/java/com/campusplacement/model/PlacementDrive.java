package com.campusplacement.model;

/**
 * Model class representing a Placement Drive conducted by a Company.
 */
public class PlacementDrive {
    private int driveId;
    private int companyId;
    private String companyName; // joined from companies table
    private String jobRole;
    private String jobDescription;
    private double packageLpa;
    private double minCgpa;
    private String eligibleDepartments;
    private int maxBacklogs;
    private String requiredSkills;
    private String applicationDeadline; // YYYY-MM-DD HH:MM
    private String driveStatus; // 'OPEN', 'IN_PROGRESS', 'CLOSED'

    public PlacementDrive() {
    }

    public PlacementDrive(int driveId, int companyId, String companyName, String jobRole, 
                          String jobDescription, double packageLpa, double minCgpa, 
                          String eligibleDepartments, int maxBacklogs, String requiredSkills, 
                          String applicationDeadline, String driveStatus) {
        this.driveId = driveId;
        this.companyId = companyId;
        this.companyName = companyName;
        this.jobRole = jobRole;
        this.jobDescription = jobDescription;
        this.packageLpa = packageLpa;
        this.minCgpa = minCgpa;
        this.eligibleDepartments = eligibleDepartments;
        this.maxBacklogs = maxBacklogs;
        this.requiredSkills = requiredSkills;
        this.applicationDeadline = applicationDeadline;
        this.driveStatus = driveStatus;
    }

    // Getters and Setters
    public int getDriveId() {
        return driveId;
    }

    public void setDriveId(int driveId) {
        this.driveId = driveId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
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

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public String getEligibleDepartments() {
        return eligibleDepartments;
    }

    public void setEligibleDepartments(String eligibleDepartments) {
        this.eligibleDepartments = eligibleDepartments;
    }

    public int getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(int maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getApplicationDeadline() {
        return applicationDeadline;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
    }

    public String getDriveStatus() {
        return driveStatus;
    }

    public void setDriveStatus(String driveStatus) {
        this.driveStatus = driveStatus;
    }

    @Override
    public String toString() {
        return companyName + " - " + jobRole + " (" + packageLpa + " LPA)";
    }
}
