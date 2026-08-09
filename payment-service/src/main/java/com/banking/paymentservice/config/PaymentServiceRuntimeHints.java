package com.banking.paymentservice.config;

import com.banking.paymentservice.DTO.CreatePaymentRequest;
import com.banking.paymentservice.DTO.PaymentCompletedEvent;
import com.banking.paymentservice.DTO.PaymentOrderResponse;
import com.banking.paymentservice.entity.Payment;
import com.banking.paymentservice.entity.enums.PaymentStatus;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class PaymentServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all DTOs and entities for Jackson serialization/deserialization
        hints.reflection()
                .registerType(CreatePaymentRequest.class, MemberCategory.values())
                .registerType(PaymentOrderResponse.class, MemberCategory.values())
                .registerType(PaymentCompletedEvent.class, MemberCategory.values())
                .registerType(Payment.class, MemberCategory.values())
                .registerType(PaymentStatus.class, MemberCategory.values())
                // Register Razorpay SDK entities and org.json for reflection in native image
                .registerType(com.razorpay.Order.class, MemberCategory.values())
                .registerType(com.razorpay.Entity.class, MemberCategory.values())
                .registerType(com.razorpay.Payment.class, MemberCategory.values())
                .registerType(com.razorpay.Refund.class, MemberCategory.values())
                .registerType(com.razorpay.Customer.class, MemberCategory.values())
                .registerType(com.razorpay.RazorpayClient.class, MemberCategory.values())
                .registerType(org.json.JSONObject.class, MemberCategory.values())
                .registerType(org.json.JSONArray.class, MemberCategory.values());

        // Register classpath resources
        hints.resources().registerPattern("schema.sql");
        hints.resources().registerPattern("data.sql");
        hints.resources().registerPattern("truststore.jks");
    }
}
