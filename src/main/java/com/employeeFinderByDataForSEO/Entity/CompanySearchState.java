package com.employeeFinderByDataForSEO.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_search_state")
public class CompanySearchState {

    @Id
    @Column(name = "company_key")
    private String companyKey;

    @Column(nullable = false)
    private String company;

    @Column(name = "next_group_index", nullable = false)
    private int nextGroupIndex = 0;

    /**
     * How many search groups IN A ROW returned zero new unique
     * profiles. Used to detect "we've run dry" before burning
     * through every remaining group in the list.
     */
    @Column(name = "consecutive_empty_groups", nullable = false)
    private int consecutiveEmptyGroups = 0;

    /**
     * Once true, we stop fetching more groups for this company
     * even if nextGroupIndex hasn't reached the end of the list -
     * saves DataForSEO calls (and cost) once returns have clearly
     * dried up.
     */
    @Column(nullable = false)
    private boolean exhausted = false;

    public CompanySearchState() {
    }

    public CompanySearchState(
            String companyKey,
            String company) {

        this.companyKey = companyKey;
        this.company = company;
        this.nextGroupIndex = 0;
        this.consecutiveEmptyGroups = 0;
        this.exhausted = false;
    }

    public String getCompanyKey() {
        return companyKey;
    }

    public String getCompany() {
        return company;
    }

    public int getNextGroupIndex() {
        return nextGroupIndex;
    }

    public int getConsecutiveEmptyGroups() {
        return consecutiveEmptyGroups;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public void setCompanyKey(String companyKey) {
        this.companyKey = companyKey;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setNextGroupIndex(int nextGroupIndex) {
        this.nextGroupIndex = nextGroupIndex;
    }

    public void setConsecutiveEmptyGroups(int consecutiveEmptyGroups) {
        this.consecutiveEmptyGroups = consecutiveEmptyGroups;
    }

    public void setExhausted(boolean exhausted) {
        this.exhausted = exhausted;
    }
}
