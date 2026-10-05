package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Subscription;
import com.employeeFinderByDataForSEO.exception.SubscriptionException;
import com.employeeFinderByDataForSEO.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private SubscriptionService subscriptionService;

    private Account account;
    private Subscription subscription;

    @BeforeEach
    void setUp() {

        subscriptionService =
                new SubscriptionService(subscriptionRepository);

        account = new Account();
        account.setId(1L);
        account.setEmail("test@example.com");

        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setAccount(account);
        subscription.setPlan(Subscription.Plan.STANDARD);
    }

    @Test
    void shouldAllowActiveSubscription() {

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(
                LocalDateTime.now().minusDays(5)
        );

        subscription.setEndDate(
                LocalDateTime.now().plusDays(25)
        );

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.of(subscription));

        assertDoesNotThrow(() ->
                subscriptionService.checkActiveSubscription(account)
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void shouldExpireSubscriptionWhenEndDatePassed() {

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(
                LocalDateTime.now().minusMonths(2)
        );

        subscription.setEndDate(
                LocalDateTime.now().minusMinutes(1)
        );

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.of(subscription));

        SubscriptionException exception =
                assertThrows(
                        SubscriptionException.class,
                        () -> subscriptionService.checkActiveSubscription(account)
                );

        assertEquals(
                "Subscription has expired",
                exception.getMessage()
        );

        assertEquals(
                Subscription.SubscriptionStatus.EXPIRED,
                subscription.getStatus()
        );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldRejectAlreadyExpiredSubscription() {

        subscription.setStatus(
                Subscription.SubscriptionStatus.EXPIRED
        );

        subscription.setStartDate(
                LocalDateTime.now().minusMonths(2)
        );

        subscription.setEndDate(
                LocalDateTime.now().minusMinutes(1)
        );

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.of(subscription));

        SubscriptionException exception =
                assertThrows(
                        SubscriptionException.class,
                        () -> subscriptionService.checkActiveSubscription(account)
                );

        assertEquals(
                "Subscription is not active",
                exception.getMessage()
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void shouldRejectWhenSubscriptionDoesNotExist() {

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.empty());

        SubscriptionException exception =
                assertThrows(
                        SubscriptionException.class,
                        () -> subscriptionService.checkActiveSubscription(account)
                );

        assertEquals(
                "No active subscription found",
                exception.getMessage()
        );

        verify(subscriptionRepository, never())
                .save(any(Subscription.class));
    }

    @Test
    void shouldReturnTrueForActiveSubscription() {

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(
                LocalDateTime.now().minusDays(1)
        );

        subscription.setEndDate(
                LocalDateTime.now().plusDays(29)
        );

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.of(subscription));

        assertTrue(
                subscriptionService.hasActiveSubscription(account)
        );
    }

    @Test
    void shouldReturnFalseForExpiredSubscription() {

        subscription.setStatus(
                Subscription.SubscriptionStatus.ACTIVE
        );

        subscription.setStartDate(
                LocalDateTime.now().minusMonths(2)
        );

        subscription.setEndDate(
                LocalDateTime.now().minusMinutes(1)
        );

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.of(subscription));

        assertFalse(
                subscriptionService.hasActiveSubscription(account)
        );

        assertEquals(
                Subscription.SubscriptionStatus.EXPIRED,
                subscription.getStatus()
        );
    }
    @Test
    void shouldCreateSubscriptionForNewAccount() {

        when(subscriptionRepository.findByAccount(account))
                .thenReturn(Optional.empty());

        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Subscription result =
                subscriptionService.createSubscription(account);

        assertNotNull(result);

        assertEquals(
                Subscription.Plan.STANDARD,
                result.getPlan()
        );

        assertEquals(
                Subscription.SubscriptionStatus.ACTIVE,
                result.getStatus()
        );

        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());

        verify(subscriptionRepository)
                .save(any(Subscription.class));
    }
}