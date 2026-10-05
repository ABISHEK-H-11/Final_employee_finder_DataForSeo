package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Subscription;
import com.employeeFinderByDataForSEO.exception.SubscriptionException;
import com.employeeFinderByDataForSEO.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createSubscription(Account account) {

        LocalDateTime now = LocalDateTime.now();

        // Check if account already has a subscription
        var existingSubscription =
                subscriptionRepository.findByAccount(account);

        if (existingSubscription.isPresent()) {

            Subscription subscription = existingSubscription.get();

            // Active subscription cannot be created again
            if (subscription.getStatus()
                    == Subscription.SubscriptionStatus.ACTIVE
                    && subscription.getEndDate().isAfter(now)) {

                throw new SubscriptionException(
                        "Account already has an active subscription"
                );
            }

            // Existing subscription has expired
            subscription.setPlan(Subscription.Plan.STANDARD);
            subscription.setStatus(
                    Subscription.SubscriptionStatus.ACTIVE
            );
            subscription.setStartDate(now);
            subscription.setEndDate(now.plusMonths(1));

            return subscriptionRepository.save(subscription);
        }

        // First subscription for the account
        Subscription subscription = new Subscription();

        subscription.setAccount(account);
        subscription.setPlan(Subscription.Plan.STANDARD);
        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusMonths(1));

        return subscriptionRepository.save(subscription);
    }

    public void checkActiveSubscription(Account account) {

        Subscription subscription = subscriptionRepository
                .findByAccount(account)
                .orElseThrow(() ->
                        new SubscriptionException(
                                "No active subscription found"
                        ));

        LocalDateTime now = LocalDateTime.now();

        if (subscription.getStatus()
                != Subscription.SubscriptionStatus.ACTIVE) {

            throw new SubscriptionException(
                    "Subscription is not active"
            );
        }

        if (subscription.getEndDate().isBefore(now)) {

            subscription.setStatus(
                    Subscription.SubscriptionStatus.EXPIRED
            );

            subscriptionRepository.save(subscription);

            throw new SubscriptionException(
                    "Subscription has expired"
            );
        }
    }

    public boolean hasActiveSubscription(Account account) {

        try {
            checkActiveSubscription(account);
            return true;

        } catch (SubscriptionException e) {
            return false;
        }
    }
}