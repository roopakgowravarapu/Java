package com.campusplacement.model;

/**
 * Model class representing a recruiting Company.
 */
public class Company {
    private int companyId;
    private String companyName;
    private String industry;
    private String location;
    private String website;
    private String hrContact;
    private String email;

    public Company() {
    }

    public Company(int companyId, String companyName, String industry, 
                   String location, String website, String hrContact, String email) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.industry = industry;
        this.location = location;
        this.website = website;
        this.hrContact = hrContact;
        this.email = email;
    }

    // Getters and Setters
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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getHrContact() {
        return hrContact;
    }

    public void setHrContact(String hrContact) {
        this.hrContact = hrContact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return companyName + " (" + industry + " - " + location + ")";
    }
}
