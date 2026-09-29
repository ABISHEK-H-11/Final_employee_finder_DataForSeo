package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.AccountUsage;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.repository.AccountUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuotaServiceTest {

    @Mock
    private AccountUsageRepository accountUsageRepository;

    @Mock
    private AccountRepository accountRepository;

    private QuotaService quotaService;

    private Account account;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        quotaService =
                new QuotaService(
                        accountUsageRepository,
                        accountRepository
                );

        account = new Account();
    }

    @Test
    void shouldConsumeProfilesAndIncreaseUsage() {

        AccountUsage usage =
                new AccountUsage(
                        9000,
                        LocalDateTime.of(2026, 9, 29, 9, 0),
                        account
                );

        Mockito.when(accountUsageRepository.findByAccount(account))
                .thenReturn(Optional.of(usage));

        quotaService.consumeProfiles(account, 100);

        assertEquals(
                9100,
                usage.getDailyProfileUsage()
        );

        Mockito.verify(accountUsageRepository)
                .save(usage);
    }

    @Test
    void shouldRejectWhenQuotaIsExceeded() {

        AccountUsage usage =
                new AccountUsage(
                        9950,
                        LocalDateTime.of(2026, 9, 29, 9, 0),
                        account
                );

        Mockito.when(accountUsageRepository.findByAccount(account))
                .thenReturn(Optional.of(usage));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> quotaService.consumeProfiles(account, 100)
        );

        assertEquals(
                9950,
                usage.getDailyProfileUsage()
        );

        Mockito.verify(accountUsageRepository, Mockito.never())
                .save(usage);
    }

    @Test
    void shouldResetUsageAtStartOfNewQuotaPeriod() {

        AccountUsage usage =
                new AccountUsage(
                        5000,
                        LocalDateTime.of(2026, 9, 28, 9, 0),
                        account
                );

        Mockito.when(accountUsageRepository.findByAccount(account))
                .thenReturn(Optional.of(usage));

        long remaining =
                quotaService.getRemainingQuota(account);

        assertEquals(10000, remaining);

        assertEquals(
                0,
                usage.getDailyProfileUsage()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 29, 9, 0),
                usage.getLastResetAt()
        );

        Mockito.verify(accountUsageRepository)
                .save(usage);
    }

    @Test
    void shouldAllowExactlyRemainingQuota() {

        AccountUsage usage =
                new AccountUsage(
                        9995,
                        LocalDateTime.of(2026, 9, 29, 9, 0),
                        account
                );

        Mockito.when(accountUsageRepository.findByAccount(account))
                .thenReturn(Optional.of(usage));

        quotaService.consumeProfiles(account, 5);

        assertEquals(
                10000,
                usage.getDailyProfileUsage()
        );

        Mockito.verify(accountUsageRepository)
                .save(usage);
    }
}