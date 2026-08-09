package com.banking.accountservice.config;

import com.banking.accountservice.DTO.FraudDetected;
import com.banking.accountservice.DTO.TransactionEvent;
import com.banking.accountservice.DTO.request.AccountRequest;
import com.banking.accountservice.DTO.request.OnboardingRequest;
import com.banking.accountservice.DTO.response.AccountResponse;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.entity.enums.AccountType;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class AccountServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all DTOs for Jackson serialization/deserialization
        hints.reflection()
                .registerType(OnboardingRequest.class, MemberCategory.values())
                .registerType(AccountRequest.class, MemberCategory.values())
                .registerType(AccountResponse.class, MemberCategory.values())
                .registerType(Account.class, MemberCategory.values())
                .registerType(AccountType.class, MemberCategory.values())
                .registerType(AccountStatus.class, MemberCategory.values())
                .registerType(FraudDetected.class, MemberCategory.values())
                .registerType(TransactionEvent.class, MemberCategory.values());

        // Register classpath resources needed at runtime
        hints.resources().registerPattern("schema.sql");
        hints.resources().registerPattern("data.sql");
        hints.resources().registerPattern("truststore.jks");
        hints.resources().registerPattern("ca.pem");
    }
}
