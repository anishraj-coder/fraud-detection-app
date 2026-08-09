package com.banking.notificationservice.config;

import com.banking.notificationservice.dto.Account;
import com.banking.notificationservice.dto.AccountEvent;
import com.banking.notificationservice.dto.EventType;
import com.banking.notificationservice.dto.OtpVerification;
import com.banking.notificationservice.dto.enums.AccountStatus;
import com.banking.notificationservice.dto.enums.AccountType;
import com.banking.notificationservice.dto.transactions.TransactionCompleted;
import com.banking.notificationservice.dto.transactions.TransactionInitiated;
import com.banking.notificationservice.dto.transactions.TransactionRefunded;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class NotificationServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register all DTOs and enums for Jackson deserialization (Kafka event payloads)
        hints.reflection()
                .registerType(Account.class, MemberCategory.values())
                .registerType(AccountEvent.class, MemberCategory.values())
                .registerType(OtpVerification.class, MemberCategory.values())
                .registerType(EventType.class, MemberCategory.values())
                .registerType(AccountStatus.class, MemberCategory.values())
                .registerType(AccountType.class, MemberCategory.values())
                .registerType(TransactionCompleted.class, MemberCategory.values())
                .registerType(TransactionInitiated.class, MemberCategory.values())
                .registerType(TransactionRefunded.class, MemberCategory.values());

        // Register classpath resources
        hints.resources().registerPattern("truststore.jks");
    }
}
