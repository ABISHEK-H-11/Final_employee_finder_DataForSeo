package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.exception.DailyQuotaExceededException;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.service.EmployeeSearchService;
import com.employeeFinderByDataForSEO.service.QuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    private EmployeeSearchService employeeSearchService;
    private AccountRepository accountRepository;
    private QuotaService quotaService;
    private Authentication authentication;

    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {

        employeeSearchService =
                mock(EmployeeSearchService.class);

        accountRepository =
                mock(AccountRepository.class);

        quotaService =
                mock(QuotaService.class);

        authentication =
                mock(Authentication.class);

        employeeController =
                new EmployeeController(
                        employeeSearchService,
                        accountRepository,
                        quotaService
                );
    }

    @Test
    void shouldRejectRequestWhenDailyQuotaIsExceeded() {

        when(authentication.getName())
                .thenReturn("abishek@example.com");

        Account account = new Account();

        when(accountRepository.findByEmail("abishek@example.com"))
                .thenReturn(java.util.Optional.of(account));

        doThrow(
                new DailyQuotaExceededException(
                        "Daily employee profile quota exceeded"
                )
        ).when(quotaService).checkQuota(account);

        assertThrows(
                DailyQuotaExceededException.class,
                () -> employeeController.search(
                        "Wipro",
                        authentication
                )
        );

        verify(employeeSearchService, never())
                .search(anyString(), anyInt());
    }
}