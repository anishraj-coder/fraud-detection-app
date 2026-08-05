package com.banking.paymentservice.DTO;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentCompletedEvent(String accountNumber, BigDecimal amount,String razorpayOrderId) {
}
