package com.employeeFinderByDataForSEO.service;

import com.employeeFinderByDataForSEO.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    public RazorpayService(
            RazorpayClient razorpayClient,
            RazorpayConfig razorpayConfig) {

        this.razorpayClient = razorpayClient;
        this.razorpayConfig = razorpayConfig;
    }

    public Order createOrder(int amount, String currency)
            throws RazorpayException {

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put(
                "receipt",
                "receipt_" + System.currentTimeMillis()
        );

        return razorpayClient.orders.create(orderRequest);
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

            return Utils.verifyPaymentSignature(
                    attributes,
                    razorpayConfig.getKeySecret()
            );

        } catch (Exception e) {

            return false;
        }
    }
}