package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Payment;
import com.employeeFinderByDataForSEO.dto.CreateOrderRequest;
import com.employeeFinderByDataForSEO.dto.PaymentVerificationRequest;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.service.RazorpayService;
import com.razorpay.RazorpayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private Authentication authentication;

    private PaymentController paymentController;

    private Account account;

    @BeforeEach
    void setUp() {

        paymentController = new PaymentController(
                razorpayService,
                accountRepository
        );

        account = new Account();
        account.setId(1L);
        account.setEmail("test@example.com");
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setPlan("STANDARD");

        Payment payment = new Payment();
        payment.setAccount(account);
        payment.setRazorpayOrderId("order_test123");
        payment.setAmount(99900);
        payment.setCurrency("INR");

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        when(razorpayService.getPlanAmount("STANDARD"))
                .thenReturn(99900);

        when(razorpayService.createOrder(
                99900,
                "INR",
                account
        )).thenReturn(payment);

        when(razorpayService.getKeyId())
                .thenReturn("rzp_test_key");

        ResponseEntity<?> response =
                paymentController.createOrder(
                        request,
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertNotNull(response.getBody());

        verify(accountRepository)
                .findByEmail("test@example.com");

        verify(razorpayService)
                .getPlanAmount("STANDARD");

        verify(razorpayService)
                .createOrder(
                        99900,
                        "INR",
                        account
                );
    }

    @Test
    void shouldVerifyPaymentSuccessfully() {

        PaymentVerificationRequest request =
                new PaymentVerificationRequest();

        request.setRazorpayOrderId("order_test123");
        request.setRazorpayPaymentId("pay_test123");
        request.setRazorpaySignature("valid-signature");

        when(razorpayService.verifyPayment(
                "order_test123",
                "pay_test123",
                "valid-signature"
        )).thenReturn(true);

        ResponseEntity<?> response =
                paymentController.verifyPayment(request);

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        assertEquals(
                "Payment verified successfully",
                response.getBody()
        );

        verify(razorpayService)
                .verifyPayment(
                        "order_test123",
                        "pay_test123",
                        "valid-signature"
                );
    }

    @Test
    void shouldRejectFailedPaymentVerification() {

        PaymentVerificationRequest request =
                new PaymentVerificationRequest();

        request.setRazorpayOrderId("order_test123");
        request.setRazorpayPaymentId("pay_test123");
        request.setRazorpaySignature("invalid-signature");

        when(razorpayService.verifyPayment(
                "order_test123",
                "pay_test123",
                "invalid-signature"
        )).thenReturn(false);

        ResponseEntity<?> response =
                paymentController.verifyPayment(request);

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Payment verification failed",
                response.getBody()
        );

        verify(razorpayService)
                .verifyPayment(
                        "order_test123",
                        "pay_test123",
                        "invalid-signature"
                );
    }

    @Test
    void shouldRejectInvalidSubscriptionPlan() throws RazorpayException {

        CreateOrderRequest request =
                new CreateOrderRequest();

        request.setPlan("GOLD");

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        when(razorpayService.getPlanAmount("GOLD"))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid subscription plan"
                        )
                );

        ResponseEntity<?> response =
                paymentController.createOrder(
                        request,
                        authentication
                );

        assertEquals(
                400,
                response.getStatusCode().value()
        );

        assertEquals(
                "Invalid subscription plan",
                response.getBody()
        );

        verify(razorpayService)
                .getPlanAmount("GOLD");

        verify(razorpayService, never())
                .createOrder(
                        anyInt(),
                        anyString(),
                        any(Account.class)
                );
    }

    @Test
    void shouldReturnErrorWhenOrderCreationFails() throws Exception {

        CreateOrderRequest request =
                new CreateOrderRequest();

        request.setPlan("STANDARD");

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        when(razorpayService.getPlanAmount("STANDARD"))
                .thenReturn(99900);

        when(razorpayService.createOrder(
                99900,
                "INR",
                account
        )).thenThrow(
                new RazorpayException("Razorpay error")
        );

        ResponseEntity<?> response =
                paymentController.createOrder(
                        request,
                        authentication
                );

        assertEquals(
                500,
                response.getStatusCode().value()
        );

        assertEquals(
                "Failed to create Razorpay order",
                response.getBody()
        );

        verify(razorpayService)
                .getPlanAmount("STANDARD");

        verify(razorpayService)
                .createOrder(
                        99900,
                        "INR",
                        account
                );
    }
}