package com.banking.accountservice.service;

import com.banking.accountservice.DTO.request.AccountRequest;
import com.banking.accountservice.DTO.request.OnboardingRequest;
import com.banking.accountservice.entity.Account;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

public interface AccountService {

    Mono<Account> getAccountByUserId(String userId);

    Mono<Map<String, BigDecimal>> getBalanceByUserId(String userId);

    Mono<Account> createAccount(AccountRequest request, String userId);

    Mono<Account> getAccountByNumber(String accountNumber);

    Mono<Void> blockAccount(String accountNumber);

    Mono<Void> unblockAccount(String accountNumber);

    Mono<Map<String, String>> creditAmount(String accountNumber, BigDecimal amount);

    Mono<Map<String, String>> debitAmount(String accountNumber, BigDecimal amount);
    Mono<Account> onboardAccount(OnboardingRequest request, String userId, String email);
}
