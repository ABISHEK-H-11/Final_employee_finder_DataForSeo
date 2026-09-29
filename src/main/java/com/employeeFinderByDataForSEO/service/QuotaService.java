package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.AccountUsage;
import com.employeeFinderByDataForSEO.exception.DailyQuotaExceededException;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.repository.AccountUsageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class QuotaService {

    private static final long DAILY_PROFILE_LIMIT = 10_000L;

    private static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final AccountUsageRepository accountUsageRepository;
    private final AccountRepository accountRepository;



    public QuotaService(AccountUsageRepository accountUsageRepository, AccountRepository accountRepository) {
        this.accountUsageRepository = accountUsageRepository;
        this.accountRepository = accountRepository;
    }

    private LocalDateTime getCurrentQuotaPeriodStart() {

        ZonedDateTime now =
                ZonedDateTime.now(BUSINESS_ZONE);

        ZonedDateTime todayReset =
                now.toLocalDate()
                        .atTime(9, 0)
                        .atZone(BUSINESS_ZONE);

        if (now.isBefore(todayReset)) {
            todayReset = todayReset.minusDays(1);
        }

        return todayReset.toLocalDateTime();
    }

    private AccountUsage getOrCreateCurrentUsage(Account account) {

        LocalDateTime currentPeriodStart =
                getCurrentQuotaPeriodStart();

        AccountUsage usage =
                accountUsageRepository.findByAccount(account)
                        .orElseGet(() -> {

                            AccountUsage newUsage =
                                    new AccountUsage(
                                            account,
                                            currentPeriodStart
                                    );

                            return accountUsageRepository.save(newUsage);
                        });

        if (usage.getLastResetAt().isBefore(currentPeriodStart)) {

            usage.setDailyProfileUsage(0);
            usage.setLastResetAt(currentPeriodStart);

            accountUsageRepository.save(usage);
        }

        return usage;
    }

    public long getRemainingQuota(Account account) {

        AccountUsage usage =
                getOrCreateCurrentUsage(account);

        return Math.max(
                0,
                DAILY_PROFILE_LIMIT
                        - usage.getDailyProfileUsage()
        );
    }

    @Transactional
    public void consumeProfiles(
            Account account,
            int profileCount) {

        if (profileCount < 0) {
            throw new IllegalArgumentException(
                    "profileCount cannot be negative"
            );
        }

        if (profileCount == 0) {
            return;
        }

        AccountUsage usage =
                getOrCreateCurrentUsage(account);

        long newUsage =
                usage.getDailyProfileUsage()
                        + profileCount;

        if (newUsage > DAILY_PROFILE_LIMIT) {
            throw new IllegalStateException(
                    "Daily profile quota exceeded"
            );
        }

        usage.setDailyProfileUsage(newUsage);

        accountUsageRepository.save(usage);
    }

    public boolean hasQuota(Account account) {
        return getRemainingQuota(account) > 0;
    }

    public void checkQuota(Account account) {

        if (!hasQuota(account)) {
            throw new DailyQuotaExceededException(
                    "Daily employee profile quota exceeded"
            );
        }
    }
}
