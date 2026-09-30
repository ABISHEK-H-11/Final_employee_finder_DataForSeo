package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.dto.EmployeeResponse;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.service.EmployeeSearchService;
import com.employeeFinderByDataForSEO.service.QuotaService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeSearchService employeeSearchService;
    private final AccountRepository accountRepository;
    private final QuotaService quotaService;

    public EmployeeController(EmployeeSearchService employeeSearchService, AccountRepository accountRepository, QuotaService quotaService) {
        this.employeeSearchService = employeeSearchService;
        this.accountRepository = accountRepository;
        this.quotaService = quotaService;
    }


    @GetMapping
    public List<EmployeeResponse> search(
            @RequestParam String company,  Authentication authentication) {
            String email = authentication.getName();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Account not found"));

        quotaService.checkQuota(account);

        long remainingQuota =
                quotaService.getRemainingQuota(account);

        int maxProfiles =
                (int) Math.min(10, remainingQuota);

        List<EmployeeResponse> results =
                employeeSearchService.search(
                        company,
                        maxProfiles
                );

        quotaService.consumeProfiles(
                account,
                results.size()
        );

        return results;

    }
}