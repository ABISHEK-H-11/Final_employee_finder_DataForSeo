package com.employeeFinderByDataForSEO.dto;

public class EmployeeResponse {

    private String name;
    private String jobTitle;
    private String company;
    private String linkedin;
    private boolean currentEmployee;
    private int confidence;

    public EmployeeResponse() {
    }

    public EmployeeResponse(String name, String jobTitle, String company,
                            String linkedin, boolean currentEmployee,
                            int confidence) {
        this.name = name;
        this.jobTitle = jobTitle;
        this.company = company;
        this.linkedin = linkedin;
        this.currentEmployee = currentEmployee;
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public boolean isCurrentEmployee() {
        return currentEmployee;
    }

    public int getConfidence() {
        return confidence;
    }
}
