package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.client.DataForSeoClient;
import com.employeeFinderByDataForSEO.dto.DataForSeoResponse;
import com.employeeFinderByDataForSEO.dto.EmployeeResponse;
import com.employeeFinderByDataForSEO.Entity.CompanySearchState;
import com.employeeFinderByDataForSEO.Entity.DiscoveredProfile;
import com.employeeFinderByDataForSEO.repository.CompanySearchStateRepository;
import com.employeeFinderByDataForSEO.repository.DiscoveredProfileRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmployeeSearchService {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeSearchService.class);

    private static final int MAX_COMPANY_LENGTH = 200;

    private final DataForSeoClient dataForSeoClient;

    private final EmployeeMapperService employeeMapperService;

    private final CompanyKeyNormalizer companyKeyNormalizer;

    private final DiscoveredProfileRepository
            discoveredProfileRepository;

    private final CompanySearchStateRepository
            companySearchStateRepository;

    /**
     * Per-company lock objects. Prevents two concurrent requests
     * for the SAME company from double-fetching, without blocking
     * requests for DIFFERENT companies (unlike a single
     * `synchronized` method, which would serialize everything).
     *
     * Note: entries are never evicted. For a long-running service
     * with many distinct companies this map will grow unbounded;
     * fine for moderate company counts, but swap for a bounded
     * cache (e.g. Caffeine) or a DB-level lock if that becomes a
     * problem.
     */
    private final Map<String, Object> companyLocks =
            new ConcurrentHashMap<>();

    /**
     * Each entry becomes its own separate DataForSEO search
     * (company + this one term) - NOT combined with each other.
     * That keeps cost linear (1 call per entry) instead of
     * exploding combinatorially (e.g. every title x every city).
     *
     * Grouped by category purely for readability; the service
     * just walks through this as one flat list, in order.
     */
    private static final List<String> JOB_TITLES = List.of(
            "",
            "Software Engineer",
            "Manager",
            "Developer",
            "Data Engineer",
            "Product Manager",
            "Cloud Engineer",
            "DevOps Engineer",
            "Business Analyst",
            "Human Resources",
            "Consultant",
            "Architect",
            "Team Lead",
            "Project Manager",
            "Sales",
            "Marketing",
            "Finance",
            "Operations",
            "Quality Engineer",
            "Support Engineer"
    );

    private static final List<String> DEPARTMENTS = List.of(
            "Engineering",
            "Information Technology",
            "Design",
            "UX",
            "Legal",
            "Research",
            "Security",
            "Recruiting",
            "Talent Acquisition",
            "Customer Support",
            "Administration",
            "Procurement",
            "Supply Chain",
            "Data Science",
            "Machine Learning",
            "Analytics"
    );

    private static final List<String> SENIORITY_LEVELS = List.of(
            "Senior",
            "Junior",
            "Lead",
            "Principal",
            "Staff",
            "Director",
            "Vice President",
            "VP",
            "Head",
            "Associate",
            "Executive",
            "Intern"
    );

    /**
     * India-focused because DataForSeoClient currently hardcodes
     * "location_name": "India" for every query. If you widen that
     * to other countries, add matching cities here too - otherwise
     * these city terms will just narrow results down to India-based
     * profiles even for a global company.
     */
    private static final List<String> CITIES = List.of(
            "Bangalore",
            "Bengaluru",
            "Hyderabad",
            "Chennai",
            "Pune",
            "Mumbai",
            "Delhi",
            "Gurgaon",
            "Gurugram",
            "Noida",
            "Kolkata"
    );

    private final List<String> searchGroups = buildSearchGroups();

    private static List<String> buildSearchGroups() {

        List<String> groups = new ArrayList<>();

        groups.addAll(JOB_TITLES);
        groups.addAll(DEPARTMENTS);
        groups.addAll(SENIORITY_LEVELS);
        groups.addAll(CITIES);

        return List.copyOf(groups);
    }

    /**
     * If this many search groups IN A ROW come back with zero new
     * unique profiles, we stop fetching further groups for that
     * company - it's a signal we've exhausted what's realistically
     * findable, and burning through the rest of a 50+ entry list
     * for no new results just wastes DataForSEO calls (and money).
     */
    private static final int EMPTY_GROUP_STOP_THRESHOLD = 8;

    public EmployeeSearchService(
            DataForSeoClient dataForSeoClient,
            EmployeeMapperService employeeMapperService,
            CompanyKeyNormalizer companyKeyNormalizer,
            DiscoveredProfileRepository discoveredProfileRepository,
            CompanySearchStateRepository companySearchStateRepository) {

        this.dataForSeoClient = dataForSeoClient;

        this.employeeMapperService =
                employeeMapperService;

        this.companyKeyNormalizer = companyKeyNormalizer;

        this.discoveredProfileRepository =
                discoveredProfileRepository;

        this.companySearchStateRepository =
                companySearchStateRepository;
    }

    /**
     * NOTE on @Transactional + locking here: the whole method is
     * annotated so Spring's proxy wraps this exact call from the
     * controller in one transaction (self-invocation of an inner
     * method would silently skip the proxy and break that). The
     * per-company `synchronized` block is nested inside, so two
     * threads hitting different companies never block each other,
     * while two threads hitting the SAME company are serialized.
     */
    @Transactional
    public List<EmployeeResponse> search(String company) {

        String cleanCompany = validateAndClean(company);

        String companyKey =
                companyKeyNormalizer.normalize(cleanCompany);

        Object lock = companyLocks.computeIfAbsent(
                companyKey,
                key -> new Object()
        );

        synchronized (lock) {

            return searchLocked(cleanCompany, companyKey);
        }
    }

    private String validateAndClean(String company) {

        if (company == null || company.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "company must not be empty"
            );
        }

        String trimmed = company.trim();

        if (trimmed.length() > MAX_COMPANY_LENGTH) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "company must be "
                            + MAX_COMPANY_LENGTH
                            + " characters or fewer"
            );
        }

        // Quotes would break out of the quoted phrase we build
        // for the DataForSEO keyword (site:linkedin.com/in/ "...").
        if (trimmed.contains("\"")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "company must not contain quote characters"
            );
        }

        return trimmed;
    }

    private List<EmployeeResponse> searchLocked(
            String cleanCompany,
            String companyKey) {

        CompanySearchState state =
                companySearchStateRepository
                        .findById(companyKey)
                        .orElseGet(() -> {

                            CompanySearchState newState =
                                    new CompanySearchState(
                                            companyKey,
                                            cleanCompany
                                    );

                            return companySearchStateRepository
                                    .save(newState);
                        });

        long availableProfiles =
                discoveredProfileRepository
                        .countByCompanyKeyAndReturnedFalse(
                                companyKey
                        );

        /*
         * Keep fetching until:
         *
         * 1. At least 10 unused profiles exist
         * OR
         * 2. All search groups are finished
         */
        while (availableProfiles < 10
                && !state.isExhausted()
                && state.getNextGroupIndex()
                < searchGroups.size()) {

            fetchAndSaveProfiles(
                    cleanCompany,
                    companyKey,
                    state
            );

            availableProfiles =
                    discoveredProfileRepository
                            .countByCompanyKeyAndReturnedFalse(
                                    companyKey
                            );
        }

        List<DiscoveredProfile> profiles =
                discoveredProfileRepository
                        .findTop10ByCompanyKeyAndReturnedFalseOrderByIdAsc(
                                companyKey
                        );

        if (profiles.isEmpty()) {

            log.info(
                    "No more profiles available for: {}",
                    cleanCompany
            );

            return List.of();
        }

        /*
         * Mark the selected profiles as returned.
         */
        for (DiscoveredProfile profile : profiles) {

            profile.setReturned(true);
        }

        discoveredProfileRepository.saveAll(profiles);

        /*
         * Convert database profiles back into
         * DataForSEO Items so your existing mapper
         * continues working.
         */
        List<DataForSeoResponse.Item> items =
                new ArrayList<>();

        for (DiscoveredProfile profile : profiles) {

            DataForSeoResponse.Item item =
                    new DataForSeoResponse.Item();

            item.setTitle(profile.getName());

            item.setDescription(
                    profile.getJobTitle()
            );

            item.setUrl(
                    profile.getLinkedinUrl()
            );

            item.setType("organic");

            items.add(item);
        }

        log.info(
                "Company: {} | Returned: {} | Remaining unused: {}",
                cleanCompany,
                profiles.size(),
                discoveredProfileRepository
                        .countByCompanyKeyAndReturnedFalse(
                                companyKey
                        )
        );

        return employeeMapperService.map(
                items,
                cleanCompany
        );
    }

    private void fetchAndSaveProfiles(
            String company,
            String companyKey,
            CompanySearchState state) {

        int groupIndex =
                state.getNextGroupIndex();

        if (groupIndex >= searchGroups.size()) {
            return;
        }

        String searchGroup =
                searchGroups.get(groupIndex);

        log.info(
                "Fetching profiles | Company: {} | Group: {}",
                company,
                searchGroup
        );

        /*
         * Move to next group before the API call.
         * This prevents repeatedly calling the same
         * group if something goes wrong later.
         */
        state.setNextGroupIndex(
                groupIndex + 1
        );

        companySearchStateRepository.save(state);

        List<DataForSeoResponse.Item> fetchedProfiles =
                dataForSeoClient.search(
                        company,
                        searchGroup
                );

        int savedCount = 0;

        for (DataForSeoResponse.Item item
                : fetchedProfiles) {

            if (item.getUrl() == null) {
                continue;
            }

            String normalizedUrl =
                    normalizeUrl(item.getUrl());

            boolean exists =
                    discoveredProfileRepository
                            .existsByCompanyKeyAndLinkedinUrl(
                                    companyKey,
                                    normalizedUrl
                            );

            if (exists) {
                continue;
            }

            DiscoveredProfile profile =
                    new DiscoveredProfile();

            profile.setName(item.getTitle());

            profile.setJobTitle(
                    item.getDescription()
            );

            profile.setCompany(company);

            profile.setCompanyKey(companyKey);

            profile.setLinkedinUrl(
                    normalizedUrl
            );

            profile.setReturned(false);

            discoveredProfileRepository
                    .save(profile);

            savedCount++;
        }

        log.info(
                "Fetched: {} | Saved new unique: {}",
                fetchedProfiles.size(),
                savedCount
        );

        if (savedCount == 0) {

            state.setConsecutiveEmptyGroups(
                    state.getConsecutiveEmptyGroups() + 1
            );

            if (state.getConsecutiveEmptyGroups()
                    >= EMPTY_GROUP_STOP_THRESHOLD) {

                state.setExhausted(true);

                log.info(
                        "Company '{}' marked exhausted after {} "
                                + "consecutive empty search groups",
                        company,
                        state.getConsecutiveEmptyGroups()
                );
            }

        } else {

            state.setConsecutiveEmptyGroups(0);
        }

        companySearchStateRepository.save(state);
    }

    private String normalizeUrl(String url) {

        String normalized =
                url.trim().toLowerCase();

        normalized = normalized
                .replace(
                        "https://www.linkedin.com/",
                        "https://linkedin.com/"
                )
                .replace(
                        "https://in.linkedin.com/",
                        "https://linkedin.com/"
                )
                .replace(
                        "https://uk.linkedin.com/",
                        "https://linkedin.com/"
                );

        int queryIndex =
                normalized.indexOf("?");

        if (queryIndex != -1) {

            normalized =
                    normalized.substring(
                            0,
                            queryIndex
                    );
        }

        while (normalized.endsWith("/")) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        return normalized;
    }
}
