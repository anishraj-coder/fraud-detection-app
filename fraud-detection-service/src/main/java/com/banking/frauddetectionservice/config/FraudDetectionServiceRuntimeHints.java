package com.banking.frauddetectionservice.config;

import com.banking.frauddetectionservice.DTO.AccountResponse;
import com.banking.frauddetectionservice.DTO.FraudCheckResult;
import com.banking.frauddetectionservice.DTO.TransactionInitiated;
import com.banking.frauddetectionservice.DTO.enums.AccountStatus;
import com.banking.frauddetectionservice.DTO.enums.AccountType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class FraudDetectionServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all DTOs and enums for Jackson serialization/deserialization
        hints.reflection()
                .registerType(AccountResponse.class, MemberCategory.values())
                .registerType(FraudCheckResult.class, MemberCategory.values())
                .registerType(TransactionInitiated.class, MemberCategory.values())
                .registerType(AccountStatus.class, MemberCategory.values())
                .registerType(AccountType.class, MemberCategory.values())
                // Fix for Spring Cloud LoadBalancer NamedContextFactory in native image
                .registerType(AnnotationConfigApplicationContext.class, MemberCategory.values());

        // Register classpath resources
        hints.resources().registerPattern("truststore.jks");
    }
}
