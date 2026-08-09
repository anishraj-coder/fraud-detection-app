package com.banking.transaction_service.config;

import com.banking.transaction_service.DTO.*;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.entity.enums.TransactionType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TransactionServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all DTOs for Jackson serialization/deserialization
        hints.reflection()
                .registerType(AccountResponse.class, MemberCategory.values())
                .registerType(CustomerTransferRequest.class, MemberCategory.values())
                .registerType(TransactionRequest.class, MemberCategory.values())
                .registerType(TransferResponseDTO.class, MemberCategory.values())
                .registerType(FraudCheckResult.class, MemberCategory.values())
                .registerType(OtpVerification.class, MemberCategory.values())
                .registerType(TransactionInitiated.class, MemberCategory.values())
                .registerType(TransactionCompleted.class, MemberCategory.values())
                .registerType(TransactionRefunded.class, MemberCategory.values())
                .registerType(Transaction.class, MemberCategory.values())
                .registerType(TransactionStatus.class, MemberCategory.values())
                .registerType(TransactionType.class, MemberCategory.values())
                // Fix for Spring Cloud LoadBalancer NamedContextFactory in native image:
                // AnnotationConfigApplicationContext must be reflectively accessible so that
                // NamedContextFactory can create per-service child contexts at runtime.
                .registerType(AnnotationConfigApplicationContext.class, MemberCategory.values());

        // Register classpath resources
        hints.resources().registerPattern("schema.sql");
        hints.resources().registerPattern("data.sql");
        hints.resources().registerPattern("truststore.jks");
    }
}
