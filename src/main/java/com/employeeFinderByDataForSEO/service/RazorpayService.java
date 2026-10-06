package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.Entity.Account;
import com.employeeFinderByDataForSEO.Entity.Payment;
import com.employeeFinderByDataForSEO.config.RazorpayConfig;
import com.employeeFinderByDataForSEO.repository.PaymentRepository;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class RazorpayService {
    private static final int STANDARD_PLAN_AMOUNT = 60000;

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    
    public RazorpayService(RazorpayClient razorpayClient, RazorpayConfig razorpayConfig, PaymentRepository paymentRepository, SubscriptionService subscriptionService) {
        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
        this.paymentRepository = paymentRepository;
        this.subscriptionService = subscriptionService;
    }
    public int getPlanAmount(String plan) {

        if ("STANDARD".equalsIgnoreCase(plan)) {
            return STANDARD_PLAN_AMOUNT;
        }

        throw new IllegalArgumentException("Invalid subscription plan");
    }
    public Payment createOrder(int amount, String currency, Account account)
            throws RazorpayException {

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put(
                "receipt",
                "receipt_" + System.currentTimeMillis()
        );
        Order razorpayOrder =
                razorpayClient.orders.create(orderRequest);

        Payment payment = new Payment();

        payment.setAccount(account);
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus("CREATED");
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        try {

            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    razorpayOrderId
            );

            attributes.put(
                    "razorpay_payment_id",
                    razorpayPaymentId
            );

            attributes.put(
                    "razorpay_signature",
                    razorpaySignature
            );

            boolean verified = Utils.verifyPaymentSignature(
                    attributes,
                    razorpayConfig.getKeySecret()
            );

            if (!verified) {
                return false;
            }

            Payment payment = paymentRepository
                    .findByRazorpayOrderId(razorpayOrderId)
                    .orElseThrow(() ->
                            new RuntimeException("Payment not found"));

            if ("SUCCESS".equals(payment.getStatus())) {
                return true;
            }

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setStatus("SUCCESS");

            paymentRepository.save(payment);

            subscriptionService.createSubscription(payment.getAccount());

            return true;

        } catch (Exception e) {

            return false;
        }

    }
    public String getKeyId() {
        return razorpayConfig.getKeyId();
    }
}