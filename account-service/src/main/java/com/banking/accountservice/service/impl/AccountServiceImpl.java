package com.banking.accountservice.service.impl;

import com.banking.accountservice.DTO.request.AccountRequest;
import com.banking.accountservice.DTO.request.OnboardingRequest;
import com.banking.accountservice.config.AccountEvent;
import com.banking.accountservice.config.AccountsProducerConfig;
import com.banking.accountservice.config.EventType;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.entity.enums.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountsProducerConfig producerConfig;

    // =========================================================================
    // CUSTOMER DOMAIN METHODS (Driven by Keycloak userId / sub claim)
    // =========================================================================

    @Override
    public Mono<Account> getAccountByUserId(String userId) {
        log.info("Fetching account for authenticated userId: {}", userId);
        return accountRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account found for user ID: " + userId)));
    }

    @Override
    public Mono<Map<String, BigDecimal>> getBalanceByUserId(String userId) {
        log.info("Fetching balance for authenticated userId: {}", userId);
        return getAccountByUserId(userId)
                .map(account -> Map.of("balance", account.getAccountBalance()));
    }

    @Override
    public Mono<Account> onboardAccount(OnboardingRequest request, String userId, String userEmail) {
        log.info("Onboarding new account for userId: {}, email: {}", userId, userEmail);

        String generatedAccountNumber = "5010" + String.format("%010d", Math.abs(UUID.randomUUID().getLeastSignificantBits() % 10000000000L));

        Account newAccount = Account.builder()
                .accountNumber(generatedAccountNumber)
                .userId(userId)
                .email(userEmail)
                .accountHolderName(request.getAccountHolderName())
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.PENDING_INITIAL_DEPOSIT) // Awaiting ₹500 initial deposit
                .phone(request.getPhone())
                .accountBalance(BigDecimal.ZERO) // Balance starts at zero until payment is captured
                .dailyTransactionLimit(request.getAccountType() == AccountType.SAVINGS ?
                        BigDecimal.valueOf(100000) : BigDecimal.valueOf(500000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNew(true)
                .build();

        return accountRepository.findByUserId(userId)
                .flatMap(existing -> Mono.<Account>error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "An account already exists for this user")))
                .switchIfEmpty(Mono.defer(() -> accountRepository.save(newAccount)))
                .doOnSuccess(saved -> log.info("Successfully registered pending account: {} for userId: {}",
                        saved.getAccountNumber(), userId));
    }

    // =========================================================================
    // ADMIN DOMAIN METHODS (Target specific user or account numbers)
    // =========================================================================

    @Override
    public Mono<Account> createAccount(AccountRequest request, String userId) {
        log.info("Creating account for target userId: {} with accountNumber: {}",
                userId, request.getAccountNumber());

        Account newAccount = Account.builder()
                .accountNumber(request.getAccountNumber())
                .userId(userId)
                .accountHolderName(request.getAccountHolderName())
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .email(request.getEmail())
                .phone(request.getPhone())
                .accountBalance(request.getInitialDeposit())
                .dailyTransactionLimit(request.getAccountType() == AccountType.SAVINGS ?
                        BigDecimal.valueOf(100000) : BigDecimal.valueOf(500000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNew(true)
                .build();

        return accountRepository.findByUserIdOrAccountNumber(userId, request.getAccountNumber())
                .flatMap(existing -> {
                    log.error("Conflict detected. Account with userId {} or accountNumber {} already exists",
                            userId, request.getAccountNumber());
                    return Mono.<Account>error(new ResponseStatusException(
                            HttpStatus.CONFLICT, "An account with the same user ID or account number already exists"));
                })
                .switchIfEmpty(Mono.defer(() -> accountRepository.save(newAccount)))
                .doOnSuccess(savedAccount -> {
                    log.info("Successfully created account: {}", savedAccount.getAccountNumber());
                    AccountEvent event = AccountEvent.builder()
                            .account(savedAccount)
                            .eventType(EventType.CREATED)
                            .build();
                    producerConfig.publish(event);
                })
                .onErrorResume(ex -> {
                    log.error("Error occurred during account creation: {}", ex.getMessage());
                    return Mono.error(ex);
                });
    }

    @Override
    public Mono<Account> getAccountByNumber(String accountNumber) {
        log.info("Fetching account by account number: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found for number: " + accountNumber)));
    }

    @Override
    public Mono<Void> blockAccount(String accountNumber) {
        log.info("Blocking account number: {}", accountNumber);
        return getAccountByNumber(accountNumber)
                .flatMap(account -> {
                    account.setNewToFalse();
                    account.setAccountStatus(AccountStatus.BLOCKED);
                    return accountRepository.save(account);
                })
                .doOnSuccess(acc -> producerConfig.publish(
                        AccountEvent.builder().account(acc).eventType(EventType.BLOCKED).build()))
                .then();
    }

    @Override
    public Mono<Void> unblockAccount(String accountNumber) {
        log.info("Unblocking account number: {}", accountNumber);
        return getAccountByNumber(accountNumber)
                .flatMap(account -> {
                    account.setNewToFalse();
                    account.setAccountStatus(AccountStatus.ACTIVE);
                    return accountRepository.save(account);
                })
                .doOnSuccess(acc -> producerConfig.publish(
                        AccountEvent.builder().account(acc).eventType(EventType.UNBLOCKED).build()))
                .then();
    }

    // =========================================================================
    // TRANSACTION & INTERNAL SETTLEMENT METHODS (Called via WebClient / Kafka)
    // =========================================================================

    @Override
    public Mono<Map<String, String>> creditAmount(String accountNumber, BigDecimal amount) {
        log.info("Processing CREDIT of {} to account: {}", amount, accountNumber);
        return getAccountByNumber(accountNumber)
                .flatMap(acc -> {
                    if (acc.getAccountStatus() != AccountStatus.ACTIVE) {
                        log.error("Cannot credit: Account {} is not ACTIVE (Status: {})",
                                accountNumber, acc.getAccountStatus());
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "Account is not active"));
                    }
                    return Mono.just(acc);
                })
                .flatMap(account -> {
                    account.setNewToFalse();
                    BigDecimal newBalance = account.getAccountBalance().add(amount);
                    account.setAccountBalance(newBalance);
                    return accountRepository.save(account);
                })
                .map(res -> Map.of(
                        "accountNumber", res.getAccountNumber(),
                        "currentBalance", res.getAccountBalance().toString()
                ));
    }

    @Override
    public Mono<Map<String, String>> debitAmount(String accountNumber, BigDecimal amount) {
        log.info("Processing DEBIT of {} from account: {}", amount, accountNumber);
        return getAccountByNumber(accountNumber)
                .flatMap(acc -> {
                    if (acc.getAccountStatus() != AccountStatus.ACTIVE) {
                        log.error("Cannot debit: Account {} is not ACTIVE (Status: {})",
                                accountNumber, acc.getAccountStatus());
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "Account is not active"));
                    }
                    return Mono.just(acc);
                })
                .flatMap(account -> {
                    if (account.getAccountBalance().compareTo(amount) < 0) {
                        log.error("Insufficient funds for account: {}. Required: {}, Available: {}",
                                accountNumber, amount, account.getAccountBalance());
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Insufficient outstanding balance"));
                    }
                    account.setNewToFalse();
                    BigDecimal newBalance = account.getAccountBalance().subtract(amount);
                    account.setAccountBalance(newBalance);
                    return accountRepository.save(account);
                })
                .map(res -> Map.of(
                        "accountNumber", res.getAccountNumber(),
                        "currentBalance", res.getAccountBalance().toString()
                ));
    }
}