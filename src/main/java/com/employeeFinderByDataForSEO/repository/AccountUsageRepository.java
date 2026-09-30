package com.employeeFinderByDataForSEO.repository;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.AccountUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountUsageRepository extends JpaRepository<AccountUsage, Long> {
    Optional<AccountUsage> findByAccount(Account account);
}
