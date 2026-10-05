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

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository) {

        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createSubscription(Account account) {

        if (subscriptionRepository.findByAccount(account).isPresent()) {
            throw new SubscriptionException(
                    "Account already has a subscription"
            );
        }

        Subscription subscription = new Subscription();

        subscription.setAccount(account);
        subscription.setPlan(Subscription.Plan.STANDARD);
        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(1);

        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);

        return subscriptionRepository.save(subscription);
    }

    public void checkActiveSubscription(Account account) {

        Subscription subscription = subscriptionRepository
                .findByAccount(account)
                .orElseThrow(() ->
                        new SubscriptionException(
                                "No active subscription found"
                        ));

        if (subscription.getStatus() !=
                Subscription.SubscriptionStatus.ACTIVE) {

            throw new SubscriptionException(
                    "Subscription is not active"
            );
        }

        if (subscription.getEndDate()
                .isBefore(LocalDateTime.now())) {

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