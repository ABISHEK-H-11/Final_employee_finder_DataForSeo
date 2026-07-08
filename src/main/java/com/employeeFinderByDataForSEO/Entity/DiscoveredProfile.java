package com.employeeFinderByDataForSEO.Entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "discovered_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_company_linkedin_url",
                        columnNames = {
                                "company_key",
                                "linkedin_url"
                        }
                )
        }
)
public class DiscoveredProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String name;

    @Column(name = "job_title", length = 2000)
    private String jobTitle;

    @Column(nullable = false)
    private String company;

    @Column(name = "company_key", nullable = false)
    private String companyKey;

    @Column(
            name = "linkedin_url",
            nullable = false,
            length = 700
    )
    private String linkedinUrl;

    @Column(nullable = false)
    private boolean returned = false;

    public DiscoveredProfile() {
    }

    public Long getId() {
        return id;
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

    public String getCompanyKey() {
        return companyKey;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCompanyKey(String companyKey) {
        this.companyKey = companyKey;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}