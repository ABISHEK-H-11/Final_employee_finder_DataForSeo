package com.employeeFinderByDataForSEO.repository;

import com.employeeFinderByDataForSEO.Entity.CompanySearchState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySearchStateRepository
        extends JpaRepository<CompanySearchState, String> {
}