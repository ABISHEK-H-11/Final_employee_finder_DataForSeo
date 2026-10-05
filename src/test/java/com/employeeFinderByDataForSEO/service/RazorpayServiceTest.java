package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Payment;
import com.employeeFinderByDataForSEO.config.RazorpayConfig;
import com.employeeFinderByDataForSEO.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.mockito.MockedStatic;
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

    @Test
    void shouldNotCreateSubscriptionForAlreadySuccessfulPayment() {

        payment.setStatus("SUCCESS");

        when(razorpayConfig.getKeySecret())
                .thenReturn("test-secret-key");

        when(paymentRepository.findByRazorpayOrderId("order_test123"))
                .thenReturn(Optional.of(payment));

        String orderId = "order_test123";
        String paymentId = "pay_test123";
        String secret = "test-secret-key";

        String signature = generateSignature(
                orderId,
                paymentId,
                secret
        );

        boolean result = razorpayService.verifyPayment(
                orderId,
                paymentId,
                signature
        );

        assertTrue(result);

        verify(paymentRepository)
                .findByRazorpayOrderId(orderId);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(subscriptionService, never())
                .createSubscription(any(Account.class));
    }

    private String generateSignature(
            String orderId,
            String paymentId,
            String secret) {

        try {
            String payload = orderId + "|" + paymentId;

            javax.crypto.Mac mac =
                    javax.crypto.Mac.getInstance("HmacSHA256");

            javax.crypto.spec.SecretKeySpec secretKey =
                    new javax.crypto.spec.SecretKeySpec(
                            secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldNotCreateSubscriptionWhenPaymentDoesNotExist() {

        when(razorpayConfig.getKeySecret())
                .thenReturn("test-secret-key");

        when(paymentRepository.findByRazorpayOrderId("unknown_order"))
                .thenReturn(Optional.empty());

        String orderId = "unknown_order";
        String paymentId = "pay_test123";
        String secret = "test-secret-key";

        String signature = generateSignature(
                orderId,
                paymentId,
                secret
        );

        boolean result = razorpayService.verifyPayment(
                orderId,
                paymentId,
                signature
        );

        assertFalse(result);

        verify(paymentRepository)
                .findByRazorpayOrderId(orderId);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(subscriptionService, never())
                .createSubscription(any(Account.class));
    }

    @Test
    void shouldVerifyPaymentAndCreateSubscription() {

        when(razorpayConfig.getKeySecret())
                .thenReturn("test-secret-key");

        when(paymentRepository.findByRazorpayOrderId("order_test123"))
                .thenReturn(Optional.of(payment));

        try (MockedStatic<Utils> mockedUtils =
                     mockStatic(Utils.class)) {

            mockedUtils.when(() ->
                    Utils.verifyPaymentSignature(
                            any(org.json.JSONObject.class),
                            anyString()
                    )
            ).thenReturn(true);

            boolean result = razorpayService.verifyPayment(
                    "order_test123",
                    "pay_test123",
                    "valid-signature"
            );

            assertTrue(result);

            assertEquals(
                    "SUCCESS",
                    payment.getStatus()
            );

            assertEquals(
                    "pay_test123",
                    payment.getRazorpayPaymentId()
            );

            verify(paymentRepository)
                    .save(payment);

            verify(subscriptionService)
                    .createSubscription(account);
        }
    }
}