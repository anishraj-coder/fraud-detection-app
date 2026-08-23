package com.banking.transaction_service.service.impl;

import com.banking.transaction_service.DTO.FraudCheckResult;
import com.banking.transaction_service.DTO.OtpVerification;
import com.banking.transaction_service.client.AccountClient;
import com.banking.transaction_service.config.OtpEventProducerConfig;
import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.service.TransactionService;
import com.banking.transaction_service.service.TransactionVerificationService;
import com.banking.transaction_service.utll.UtilityClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionVerificationServiceImpl implements TransactionVerificationService {

    private final TransactionService transactionService;
    private final UtilityClass utilityClass;
    private final TransactionRepository transactionRepository;
    private final OtpEventProducerConfig otpEventProducerConfig;
    private final ReactiveRedisTemplate<String,String> reactiveRedisTemplate;
    private final AccountClient accountClient;
    private static final String SINGLE_TXN_KEY_PREFIX = "txn:ref:";

    /**
     * Consume the result from consumer and initiate the verification through 6 digit otp
     * if the transaction is not at processing state then skip it as it was completed or flagged
     * --To maintain idempotence
     * @param result
     */
    @Override
    public Mono<Void> initiateVerification(FraudCheckResult result) {
        log.info(">> Initiation of OTP verification for Transaction Ref No: {}", result.referenceId());
        return transactionService
                .getTransactionByReferenceNumber(result.referenceId())
                .onErrorResume(ex -> {
                    log.error("Failed to find transaction for ref: {}: {}", result.referenceId(), ex.getMessage());
                    return Mono.error(ex);
                })
                .flatMap(transaction -> {
                    // Skip only if transaction is already in a terminal or verification state
                    if (transaction.getStatus() == TransactionStatus.COMPLETED ||
                            transaction.getStatus() == TransactionStatus.FAILED ||
                            transaction.getStatus() == TransactionStatus.FAILED_REFUNDED ||
                            transaction.getStatus() == TransactionStatus.PENDING_VERIFICATION) {
                        log.info("Transaction {} already in status {}. Skipping duplicate OTP initiation.",
                                result.referenceId(), transaction.getStatus());
                        return Mono.empty();
                    }

                    String otpKey = "verification:otp:" + result.referenceId();
                    String otp = utilityClass.generateOtp();

                    log.info(">> [OTP Engine] Generated OTP: {} for Ref No: {}", otp, result.referenceId());
                    return reactiveRedisTemplate.opsForValue()
                            .set(otpKey, otp, Duration.ofMinutes(10))
                            .then(Mono.defer(() -> {
                                transaction.setNewToFalse();
                                transaction.setStatus(TransactionStatus.PENDING_VERIFICATION);
                                log.info("Updating transaction status to PENDING_VERIFICATION for Ref: {}", result.referenceId());
                                return transactionRepository.save(transaction);
                            }))
                            .then(Mono.defer(() -> accountClient.getAccountByNumber(transaction.getSenderAccountNumber())))
                            .flatMap(accountResponse -> {
                                OtpVerification verification = OtpVerification.builder()
                                        .referenceId(transaction.getReferenceNumber())
                                        .email(accountResponse.getEmail())
                                        .amount(transaction.getAmount())
                                        .otp(otp)
                                        .build();
                                log.info("Publishing OTP verification event for Ref: {}", result.referenceId());
                                otpEventProducerConfig.publishOtpVerificationEvent(verification);
                                return Mono.empty();
                            });
                })
                .then();
    }


    @Override
    public Mono<Map<String, String>> verifyOtp(String refId, String otp) {
        String key = "verification:otp:" + refId;
        return reactiveRedisTemplate.opsForValue().get(key)
                .flatMap(value -> {
                    if (value.equals(otp)) {
                        log.info(">> OTP verified successfully for Ref: {}. Proceeding with transaction completion.", refId);
                        Map<String, String> map = new HashMap<>();
                        map.put("ReferenceNo", refId);
                        map.put("Status", "The otp verification was successful and processing the transaction");

                        return reactiveRedisTemplate.delete(key)
                                .then(transactionService.completeTransaction(refId))
                                .then(Mono.just(map));
                    } else {
                        log.warn(">> Invalid OTP submitted for Ref: {}. Compensating transaction and blocking account.", refId);
                        return reactiveRedisTemplate.delete(key)
                                .then(transactionService.getTransactionByReferenceNumber(refId))
                                .flatMap(transaction ->
                                        transactionService.compensateTransaction(refId)
                                                .then(accountClient.blockAccount(transaction.getSenderAccountNumber()))
                                                .onErrorResume(ex -> {
                                                    log.error("Failed to block account after compensation for Ref {}: {}", refId, ex.getMessage());
                                                    return Mono.empty();
                                                })
                                )
                                .then(Mono.defer(() -> {
                                    Map<String, String> map = new HashMap<>();
                                    map.put("ReferenceNo", refId);
                                    map.put("Status", "Invalid OTP. Transaction canceled, account blocked, and debited funds refunded.");
                                    return Mono.just(map);
                                }));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Check if the transaction was already completed or flagged (e.g. via prefetch or double click)
                    return transactionService.getTransactionByReferenceNumber(refId)
                            .flatMap(transaction -> {
                                if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                                    log.info(">> Transaction {} is already COMPLETED. Returning success.", refId);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("ReferenceNo", refId);
                                    map.put("Status", "The transaction has already been verified and completed successfully!");
                                    return Mono.just(map);
                                }
                                if (transaction.getStatus() == TransactionStatus.FLAGGED) {
                                    log.info(">> Transaction {} is already FLAGGED. Returning cancellation status.", refId);
                                    Map<String, String> map = new HashMap<>();
                                    map.put("ReferenceNo", refId);
                                    map.put("Status", "Invalid OTP. Transaction has been canceled, account blocked, and debited funds refunded.");
                                    return Mono.just(map);
                                }
                                log.warn("OTP Expired or Invalid for Ref No: {}", refId);
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "OTP expired or invalid"));
                            })
                            .onErrorResume(ex -> {
                                if (ex instanceof ResponseStatusException) {
                                    return Mono.error(ex);
                                }
                                log.warn("OTP Expired or Invalid for Ref No: {}", refId);
                                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "OTP expired or invalid"));
                            });
                }));
    }



}
