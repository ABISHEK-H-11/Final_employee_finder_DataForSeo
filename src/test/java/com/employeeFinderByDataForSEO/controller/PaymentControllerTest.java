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
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

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

        request.setAmount(999);

        Payment payment = new Payment();
        payment.setAccount(account);
        payment.setRazorpayOrderId("order_test123");
        payment.setAmount(999);
        payment.setCurrency("INR");

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        when(razorpayService.createOrder(
                999,
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

        assertEquals(200, response.getStatusCode().value());

        assertNotNull(response.getBody());

        verify(accountRepository)
                .findByEmail("test@example.com");

        verify(razorpayService)
                .createOrder(999, "INR", account);
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
    void shouldReturnErrorWhenOrderCreationFails() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest();
        request.setAmount(999);

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(account));

        when(razorpayService.createOrder(
                999,
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
                .createOrder(999, "INR", account);
    }
}