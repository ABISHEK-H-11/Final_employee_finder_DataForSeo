package com.employeeFinderByDataForSEO.controller;

import com.employeeFinderByDataForSEO.dto.CreateOrderRequest;
import com.employeeFinderByDataForSEO.dto.PaymentVerificationRequest;
import com.employeeFinderByDataForSEO.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final RazorpayService razorpayService;

    public PaymentController(RazorpayService razorpayService) {
        this.razorpayService = razorpayService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest request) {

        try {
            Order order = razorpayService.createOrder(
                    request.getAmount(),
                    "INR"
            );

            Map<String, Object> response = new HashMap<>();

            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));

            return ResponseEntity.ok(response);

        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError()
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