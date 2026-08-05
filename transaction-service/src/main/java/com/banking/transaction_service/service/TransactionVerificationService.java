package com.banking.transaction_service.service;

import com.banking.transaction_service.DTO.FraudCheckResult;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface TransactionVerificationService {
    Mono<Void> initiateVerification(FraudCheckResult result);

    Mono<Map<String,String >> verifyOtp(String refId, String otp);
}
