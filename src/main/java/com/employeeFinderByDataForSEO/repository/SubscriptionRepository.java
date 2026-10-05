package com.employeeFinderByDataForSEO.repository;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByAccount(Account account);

    Optional<Subscription> findByAccountAndStatus(
            Account account,
            Subscription.SubscriptionStatus status
    );
}