package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.dto.DataForSeoResponse;
import com.employeeFinderByDataForSEO.dto.EmployeeResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeMapperService {

    /**
     * Phrases in a LinkedIn snippet that strongly indicate the
     * person no longer works there (a past role, not a current
     * one). Checked against title + description.
     */
    private static final List<String> PAST_ROLE_SIGNALS = List.of(
            "former ",
            "ex-",
            "ex ",
            "previously at",
            "worked at",
            "no longer at",
            "alumni"
    );

    /**
     * Phrases that strongly indicate a *current* role, used to
     * counteract an ambiguous match.
     */
    private static final List<String> CURRENT_ROLE_SIGNALS = List.of(
            "present",
            "current",
            "currently"
    );

    /**
     * LinkedIn search-result titles are usually one of:
     *   "Name - Job Title - Company | LinkedIn"
     *   "Name - Job Title | LinkedIn"
     *   "Name | LinkedIn"
     * This pattern strips the trailing "| LinkedIn" (and any
     * variant casing/spacing) before splitting on " - ".
     */
    private static final Pattern LINKEDIN_SUFFIX =
            Pattern.compile("\\s*\\|\\s*linkedin\\s*$",
                    Pattern.CASE_INSENSITIVE);

    public List<EmployeeResponse> map(
            List<DataForSeoResponse.Item> items,
            String company) {

        return items.stream()
                .map(item -> mapOne(item, company))
                .toList();
    }

    private EmployeeResponse mapOne(
            DataForSeoResponse.Item item,
            String company) {

        String cleanedTitle = stripLinkedInSuffix(item.getTitle());

        String name = extractName(cleanedTitle);
        String jobTitle = extractJobTitle(cleanedTitle, company);

        boolean currentEmployee =
                isCurrentEmployee(item, company);

        int confidence =
                calculateConfidence(item, company, currentEmployee);

        return new EmployeeResponse(
                name,
                jobTitle,
                company,
                item.getUrl(),
                currentEmployee,
                confidence
        );
    }

    private String stripLinkedInSuffix(String title) {

        if (title == null) {
            return "";
        }

        Matcher matcher = LINKEDIN_SUFFIX.matcher(title.trim());

        return matcher.replaceAll("").trim();
    }

    private String extractName(String cleanedTitle) {

        if (cleanedTitle.isBlank()) {
            return "Unknown";
        }

        String[] parts = cleanedTitle.split(" - ", 2);

        String name = parts[0].trim();

        return name.isBlank() ? "Unknown" : name;
    }

    /**
     * Pulls the job title segment out of "Name - Job Title - Company".
     * If the company name appears as the last " - " segment, that
     * segment is dropped so it isn't mistaken for the job title.
     */
    private String extractJobTitle(String cleanedTitle, String company) {

        if (!cleanedTitle.contains(" - ")) {
            return "Unknown";
        }

        String[] parts = cleanedTitle.split(" - ");

        if (parts.length < 2) {
            return "Unknown";
        }

        int endIndex = parts.length;

        String lastSegment = parts[parts.length - 1].trim();

        if (company != null
                && lastSegment.toLowerCase()
                        .contains(company.toLowerCase())) {

            endIndex = parts.length - 1;
        }

        if (endIndex <= 1) {
            return "Unknown";
        }

        StringBuilder jobTitle = new StringBuilder();

        for (int i = 1; i < endIndex; i++) {

            if (jobTitle.length() > 0) {
                jobTitle.append(" - ");
            }

            jobTitle.append(parts[i].trim());
        }

        String result = jobTitle.toString()
                .replace("...", "")
                .trim();

        return result.isBlank() ? "Unknown" : result;
    }

    /**
     * Heuristic only - we don't have real employment records, just
     * a Google snippet of the LinkedIn profile. Logic:
     *
     *  1. If the text contains an explicit "past role" signal
     *     (e.g. "Former", "Ex-", "previously at") -> not current.
     *  2. Else if it mentions the company at all -> treat as
     *     current, with extra confidence if an explicit "Present"
     *     / "Current" signal is also found.
     *  3. Otherwise -> not current (company isn't even mentioned).
     */
    private boolean isCurrentEmployee(
            DataForSeoResponse.Item item,
            String company) {

        String text = (safe(item.getTitle()) + " "
                + safe(item.getDescription())).toLowerCase();

        String companyLower = company.toLowerCase();

        if (!text.contains(companyLower)) {
            return false;
        }

        for (String signal : PAST_ROLE_SIGNALS) {

            if (text.contains(signal)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasExplicitCurrentSignal(
            DataForSeoResponse.Item item) {

        String text = (safe(item.getTitle()) + " "
                + safe(item.getDescription())).toLowerCase();

        for (String signal : CURRENT_ROLE_SIGNALS) {

            if (text.contains(signal)) {
                return true;
            }
        }

        return false;
    }

    private int calculateConfidence(
            DataForSeoResponse.Item item,
            String company,
            boolean currentEmployee) {

        int score = 40;

        if (item.getUrl() != null &&
                item.getUrl().contains("linkedin.com/in/")) {
            score += 20;
        }

        if (currentEmployee) {
            score += 20;
        }

        if (currentEmployee && hasExplicitCurrentSignal(item)) {
            score += 10;
        }

        if (item.getDescription() != null &&
                item.getDescription()
                        .toLowerCase()
                        .contains("experience: " +
                                company.toLowerCase())) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
