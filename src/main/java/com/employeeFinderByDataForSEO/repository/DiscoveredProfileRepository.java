package com.employeeFinderByDataForSEO.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.employeeFinderByDataForSEO.Entity.DiscoveredProfile;

import java.util.List;

public interface DiscoveredProfileRepository
        extends JpaRepository<DiscoveredProfile, Long> {

    boolean existsByCompanyKeyAndLinkedinUrl(
            String companyKey,
            String linkedinUrl
    );

    List<DiscoveredProfile>
            findTop10ByCompanyKeyAndReturnedFalseOrderByIdAsc(
                    String companyKey
            );

    long countByCompanyKeyAndReturnedFalse(
            String companyKey
    );
}