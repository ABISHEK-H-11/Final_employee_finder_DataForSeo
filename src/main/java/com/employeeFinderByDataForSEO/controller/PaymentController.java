package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Payment;
import com.employeeFinderByDataForSEO.dto.CreateOrderRequest;
import com.employeeFinderByDataForSEO.dto.PaymentVerificationRequest;
import com.employeeFinderByDataForSEO.repository.AccountRepository;
import com.employeeFinderByDataForSEO.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final RazorpayService razorpayService;
    private final AccountRepository accountRepository;

    public PaymentController(RazorpayService razorpayService, AccountRepository accountRepository) {
        this.razorpayService = razorpayService;
        this.accountRepository = accountRepository;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        try {

            String email = authentication.getName();

            Account account = accountRepository
                    .findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("Account not found"));

            int amount = razorpayService.getPlanAmount(request.getPlan());

            Payment payment = razorpayService.createOrder(
                    amount,
                    "INR",
                    account
            );

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "orderId",
                    payment.getRazorpayOrderId()
            );

            response.put(
                    "amount",
                    payment.getAmount()
            );

            response.put(
                    "currency",
                    payment.getCurrency()
            );
            response.put("keyId", razorpayService.getKeyId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (RazorpayException e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Failed to create Razorpay order");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerificationRequest request) {

        boolean verified = razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!verified) {
            return ResponseEntity.badRequest()
                    .body("Payment verification failed");
        }

        return ResponseEntity.ok("Payment verified successfully");
    }


}