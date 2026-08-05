package com.banking.paymentservice.repository;

import com.banking.paymentservice.entity.Payment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<Payment,String> {

    Mono<Payment> findByRazorPaymentId(String razorpayId);
}
