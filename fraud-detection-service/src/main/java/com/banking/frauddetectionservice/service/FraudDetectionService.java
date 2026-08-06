package com.banking.frauddetectionservice.service;

import com.banking.frauddetectionservice.DTO.AccountResponse;
import com.banking.frauddetectionservice.DTO.FraudCheckResult;
import com.banking.frauddetectionservice.DTO.TransactionInitiated;
import com.banking.frauddetectionservice.config.FraudProducerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final FraudProducerConfig fraudProducerConfig;

    @Value("${fraud.max-count:5}")
    private int MAX_VELOCITY;
    @Value("${fraud.max-multiplier:4}")
    private int MAX_MULTIPLIER;
    @Value("${fraud.max-balance-percent:90}")
    private double MAX_BALANCE_PERCENT;

    /**
     * Checks the transaction and triggers Kafka publisher based on result
     */
    public Mono<Void> fraudCheckResult(TransactionInitiated transactionInit) {
        String accountNumber = transactionInit.senderAccountNumber();
        BigDecimal amount = transactionInit.amount();
        String refNo = transactionInit.referenceId();

        log.info(">> [Fraud Engine] Processing fraud checks for transaction Ref: {}, account: {}, amount: {}",
                refNo, accountNumber, amount);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/{id}").build(accountNumber))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(
                                        clientResponse.statusCode(), "Account Fetch failed: " + errorBody))))
                .bodyToMono(AccountResponse.class)
                .flatMap(accountResponse -> {
                    log.info("Successfully fetched account details for accountNumber: {}", accountNumber);
                    log.info(">>> Performing dynamic fraud evaluation checks...");
                    return performFraudCheck(accountNumber, amount, accountResponse, refNo);
                })
                .flatMap(fraudCheckResult -> {
                    if (fraudCheckResult.result()) {
                        log.warn("[FRAUD DETECTED]Fraud detected for Ref: {}. Publishing to verify-transaction topic.", refNo);
                        fraudProducerConfig.publishVerificationRequired(fraudCheckResult);
                    } else {
                        log.info("[CLEAN TRANSACTION] Transaction Ref: {} is CLEAN. Publishing to fraud-check-clean topic.", refNo);
                        fraudProducerConfig.publishCleanTransaction(fraudCheckResult);
                    }
                    return Mono.empty();
                })
                .doOnError(ex -> log.error("[ACCOUNT SERVICE ERROR]Error during fraud evaluation pipeline for Ref: {}: {}", refNo, ex.getMessage(), ex))
                .then();
    }

    /**
     * Executes parallel fraud checks (Velocity, Suspicious Amount, Balance Percentage)
     */
    private Mono<FraudCheckResult> performFraudCheck(String accountNumber, BigDecimal amount, AccountResponse account, String refNo) {
        return Mono.zip(
                velocityCheck(accountNumber),
                isAmountSuspicious(accountNumber, amount),
                isBalanceCheckFailed(account, amount)
        ).flatMap(tuple -> {
            Boolean isVelocityCheckFailed = tuple.getT1();
            Boolean isAmountSuspicious = tuple.getT2();
            Boolean isBalanceFailed = tuple.getT3();

            boolean isFraud = isVelocityCheckFailed || isAmountSuspicious || isBalanceFailed;
            List<String> reasons = new ArrayList<>();
            reasons.add("Transaction is Clean");

            if (isFraud) {
                log.warn("Fraud evaluation flagged transaction Ref: {}", refNo);
                reasons = new ArrayList<>();
                if (isVelocityCheckFailed) reasons.add("Transaction velocity limit exceeded");
                if (isAmountSuspicious) reasons.add("Amount is significantly higher than historical average");
                if (isBalanceFailed) reasons.add("Transaction exceeds maximum allowed balance percentage threshold");
            }

            String reason = reasons.stream().collect(Collectors.joining(";"));
            return Mono.just(FraudCheckResult.builder()
                    .result(isFraud)
                    .referenceId(refNo)
                    .reason(reason)
                    .build());
        });
    }

    /**
     * Checks rate/velocity of transactions per account window in Redis
     */
    private Mono<Boolean> velocityCheck(String accountNumber) {
        String key = "fraud:velocity:" + accountNumber;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        log.info(">> First transaction in window, setting 1 minute expiration key");
                        //Currently it returns true in dev just to reproduce unhappy flow of application and later will be
                        //changed to false
                        return redisTemplate.expire(key, Duration.ofMinutes(1)).thenReturn(false);
                    }
                    if (count > MAX_VELOCITY) {
                        log.warn("Velocity check failed for account: {}. Count: {}", accountNumber, count);
                        return Mono.just(true);
                    }
                    log.info(">> Velocity Check passed");
                    return Mono.just(false);
                });
    }

    /**
     * Checks if the amount is larger than historical average multiple
     */
    private Mono<Boolean> isAmountSuspicious(String accountNumber, BigDecimal amount) {
        String avgKey = "fraud:average:" + accountNumber;
        return redisTemplate.opsForValue().get(avgKey)
                .flatMap(res -> {
                    BigDecimal avgAmount = new BigDecimal(res.toString());
                    BigDecimal newAvg = avgAmount.add(amount).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN);
                    if (amount.compareTo(newAvg.multiply(BigDecimal.valueOf(MAX_MULTIPLIER))) >= 0) {
                        log.warn(">> Suspicious Transfer detected of more than {}X of average amount: {}", MAX_MULTIPLIER, amount);
                        return Mono.just(true);
                    }
                    log.info(">> Suspicious Amount check passed");
                    return Mono.just(false);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info(">> First transaction average check: saving initial amount into Redis");
                    return redisTemplate.opsForValue().set(avgKey, amount.toString())
                            .thenReturn(false);
                }));
    }

    /**
     * Checks if transaction exceeds allowable balance percentage
     */
    private Mono<Boolean> isBalanceCheckFailed(AccountResponse accountResponse, BigDecimal amount) {
        BigDecimal maxAllowed = accountResponse.getAccountBalance()
                .multiply(BigDecimal.valueOf(MAX_BALANCE_PERCENT))
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_DOWN);
        if (maxAllowed.compareTo(amount) <= 0) {
            log.warn(">> Amount and balance check Failed. Max Allowed: {}, Requested: {}", maxAllowed, amount);
            return Mono.just(true);
        }
        log.info(">> Amount and Balance check passed");
        return Mono.just(false);
    }
}
