package com.banking.transaction_service.DTO;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OtpVerification(String referenceId,String email, BigDecimal amount,String otp) {
}
