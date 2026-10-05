package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.dto.EmployeeResponse;
import com.employeeFinderByDataForSEO.exception.SubscriptionException;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.service.EmployeeSearchService;
import com.employeeFinderByDataForSEO.service.QuotaService;
import com.employeeFinderByDataForSEO.service.SubscriptionService;
import com.employeeFinderByDataForSEO.exception.DailyQuotaExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeSearchService employeeSearchService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private QuotaService quotaService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private Authentication authentication;

    private EmployeeController employeeController;

    private Account account;

    @BeforeEach
    void setUp() {

        employeeController = new EmployeeController(
                employeeSearchService,
                accountRepository,
                quotaService,
                subscriptionService
        );

        account = new Account(
                1L,
                "testuser",
                "password",
                null,
                "test@example.com"
        );
    }

    @Test
    void shouldReturnEmployeesSuccessfully() {

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        doNothing()
                .when(subscriptionService)
                .checkActiveSubscription(account);

        when(quotaService.getRemainingQuota(account))
                .thenReturn(100L);

        List<EmployeeResponse> employees = List.of(
                new EmployeeResponse()
        );

        when(employeeSearchService.search("Wipro", 10))
                .thenReturn(employees);

        List<EmployeeResponse> result =
                employeeController.search(
                        "Wipro",
                        authentication
                );

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(subscriptionService)
                .checkActiveSubscription(account);

        verify(quotaService)
                .checkQuota(account);

        verify(employeeSearchService)
                .search("Wipro", 10);

        verify(quotaService)
                .consumeProfiles(account, 1);
    }

    @Test
    void shouldRejectWhenDailyQuotaExceeded() {

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        doNothing()
                .when(subscriptionService)
                .checkActiveSubscription(account);

        doThrow(new DailyQuotaExceededException(
                "Daily quota exceeded"
        ))
                .when(quotaService)
                .checkQuota(account);

        assertThrows(
                DailyQuotaExceededException.class,
                () -> employeeController.search(
                        "Wipro",
                        authentication
                )
        );

        verify(subscriptionService)
                .checkActiveSubscription(account);

        verify(quotaService)
                .checkQuota(account);

        verify(employeeSearchService, never())
                .search(anyString(), anyInt());
    }

    @Test
    void shouldRejectWhenSubscriptionIsExpired() {

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        doThrow(new SubscriptionException(
                "Subscription has expired"
        ))
                .when(subscriptionService)
                .checkActiveSubscription(account);

        assertThrows(
                SubscriptionException.class,
                () -> employeeController.search(
                        "Wipro",
                        authentication
                )
        );

        verify(subscriptionService)
                .checkActiveSubscription(account);

        verify(quotaService, never())
                .checkQuota(account);

        verify(employeeSearchService, never())
                .search(anyString(), anyInt());
    }

    @Test
    void shouldRejectWhenSubscriptionDoesNotExist() {

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        doThrow(new SubscriptionException(
                "No active subscription found"
        ))
                .when(subscriptionService)
                .checkActiveSubscription(account);

        assertThrows(
                SubscriptionException.class,
                () -> employeeController.search(
                        "Wipro",
                        authentication
                )
        );

        verify(subscriptionService)
                .checkActiveSubscription(account);

        verify(quotaService, never())
                .checkQuota(account);

        verify(employeeSearchService, never())
                .search(anyString(), anyInt());
    }
}