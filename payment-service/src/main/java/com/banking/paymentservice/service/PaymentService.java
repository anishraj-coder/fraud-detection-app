package com.banking.paymentservice.service;


import com.banking.paymentservice.DTO.CreatePaymentRequest;
import com.banking.paymentservice.DTO.PaymentOrderResponse;
import reactor.core.publisher.Mono;


public interface PaymentService {
    Mono<PaymentOrderResponse> createPayment(CreatePaymentRequest request);

    Mono<Void> handleWebhook(String payload, String signature);
}
