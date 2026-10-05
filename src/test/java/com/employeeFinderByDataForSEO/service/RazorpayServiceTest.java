package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Payment;
import com.employeeFinderByDataForSEO.config.RazorpayConfig;
import com.employeeFinderByDataForSEO.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RazorpayServiceTest {

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private RazorpayConfig razorpayConfig;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private RazorpayService razorpayService;

    private Account account;
    private Payment payment;

    @BeforeEach
    void setUp() {

        razorpayService = new RazorpayService(
                razorpayClient,
                razorpayConfig,
                paymentRepository,
                subscriptionService
        );

        account = new Account();
        account.setId(1L);
        account.setEmail("test@example.com");

        payment = new Payment();
        payment.setAccount(account);
        payment.setRazorpayOrderId("order_test123");
        payment.setStatus("CREATED");
    }

    @Test
    void shouldRejectInvalidPaymentSignature() {

        when(razorpayConfig.getKeySecret())
                .thenReturn("test-secret-key");

        boolean result = razorpayService.verifyPayment(
                "order_test123",
                "pay_test123",
                "invalid-signature"
        );

        assertFalse(result);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(subscriptionService, never())
                .createSubscription(any(Account.class));
    }
}