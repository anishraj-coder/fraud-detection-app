package com.banking.transaction_service.service.impl;

import com.banking.transaction_service.DTO.CustomerTransferRequest;
import com.banking.transaction_service.DTO.TransactionRequest;
import com.banking.transaction_service.DTO.TransferResponseDTO;
import com.banking.transaction_service.client.AccountClient;
import com.banking.transaction_service.config.SagaStreamBroadcaster;
import com.banking.transaction_service.config.TransactionProducerConfig;
import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.entity.enums.TransactionType;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.service.TransactionService;
import com.banking.transaction_service.utll.UtilityClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final WebClient webClient;
    private final UtilityClass utilityClass;
    private final TransactionRepository transactionRepository;
    private final TransactionProducerConfig transactionProducer;
    private final AccountClient accountClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final SagaStreamBroadcaster sagaStreamBroadcaster;
    private static final String SINGLE_TXN_KEY_PREFIX = "txn:ref:";

    private static final String CACHE_KEY_PREFIX = "txn:history:";
    private static final java.time.Duration CACHE_TTL = java.time.Duration.ofMinutes(10);


    @Override
    public Mono<Transaction> transferMoneyForCustomer(CustomerTransferRequest request) {
        log.info(">> [Customer Transfer] Initiating transfer to receiver account: {}", request.getReceiverAccountNumber());

        return accountClient.getMyAccount()
                .flatMap(account -> {
                    String senderAccountNumber = account.getAccountNumber();

                    if (senderAccountNumber.equalsIgnoreCase(request.getReceiverAccountNumber())) {
                        log.error("Transfer rejected: Sender and receiver account numbers are identical ({})", senderAccountNumber);
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Cannot transfer funds to your own account via transfer service"));
                    }

                    TransactionRequest adminRequest = TransactionRequest.builder()
                            .senderAccountNumber(senderAccountNumber)
                            .receiverAccountNumber(request.getReceiverAccountNumber())
                            .amount(request.getAmount())
                            .description(request.getDescription())
                            .build();

                    log.info(">> Resolved sender account: {} for authenticated customer", senderAccountNumber);
                    return transferMoney(adminRequest);
                });
    }

    @Override
    public Mono<Transaction> transferMoney(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transfer amount"));
        }

        String referenceNumber = utilityClass.generateReferenceNumber();

        log.info("SAGA start -> Transfer ref {}: from {} to {} amount: {}",
                referenceNumber, request.getSenderAccountNumber(), request.getReceiverAccountNumber(), request.getAmount());

        Transaction initialTransaction = Transaction.builder()
                .id(UUID.randomUUID().toString())
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .description(request.getDescription())
                .type(TransactionType.TRANSFER)
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .senderAccountNumber(request.getSenderAccountNumber())
                .amount(request.getAmount())
                .referenceNumber(referenceNumber)
                .status(TransactionStatus.PENDING)
                .build();

        return transactionRepository.save(initialTransaction)
                .flatMap(tx -> executeInternalDebit(request, referenceNumber)
                        .flatMap(debitRes -> {
                            log.info("Successfully debited sender account: {}", request.getSenderAccountNumber());
                            tx.setStatus(TransactionStatus.DEBITED);
                            tx.setNewToFalse();

                            // AFTER (Fixed Mono Chain):
                            return transactionRepository.save(tx)
                                    .flatMap(savedTx -> {
                                        notifySagaStream(savedTx); // 1. Stream SSE event (DEBITED)
                                        return evictCaches(referenceNumber, request.getSenderAccountNumber(), request.getReceiverAccountNumber())
                                                .then(Mono.fromRunnable(()->transactionProducer.publishInitiation(savedTx)))
                                                .thenReturn(savedTx); // 2. Publish Kafka Event to Fraud Detection!
                                    });

                        })
                        .onErrorResume(debitEx -> handleDebitFailure(tx, debitEx))
                );
    }

    private Mono<Map<String, String>> executeInternalDebit(TransactionRequest request, String referenceNumber) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/internal/accounts/{id}/debit")
                        .queryParam("amount", request.getAmount())
                        .build(request.getSenderAccountNumber()))
                .header("X-Idempotency-Key", referenceNumber)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, body)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Account service unavailable")))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

    private Mono<Transaction> handleDebitFailure(Transaction tx, Throwable ex) {
        log.error("Debit failed for transaction {}: {}", tx.getReferenceNumber(), ex.getMessage());
        tx.setStatus(TransactionStatus.FAILED);
        tx.setFailureReason(ex.getMessage());
        tx.setNewToFalse();

        return transactionRepository.save(tx)
                .flatMap(savedTx -> {
                    notifySagaStream(savedTx);
                    return Mono.error(ex);
                });
    }

    @Transactional
    @Override
    public Mono<Transaction> getTransactionByReferenceNumber(String referenceNumber) {
        String cacheKey = SINGLE_TXN_KEY_PREFIX + referenceNumber;

        // 1. Fetch single transaction from Redis using Object template + cast
        return redisTemplate.opsForValue().get(cacheKey)
                .cast(Transaction.class)
                .switchIfEmpty(
                        // 2. Cache miss: Fetch from DB, save to Redis
                        transactionRepository.findByReferenceNumber(referenceNumber)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "The transaction with reference number is not found")))
                                .flatMap(tx -> redisTemplate.opsForValue().set(cacheKey, tx)
                                        .then(redisTemplate.expire(cacheKey, CACHE_TTL))
                                        .thenReturn(tx))
                )
                .onErrorResume(ex -> {
                    log.error("Error fetching transaction by Reference Number: {}", referenceNumber, ex);
                    return Mono.error(ex);
                });
    }

    @Override
    public Flux<Transaction> getTransactionHistoryByAccountNumber(String accountNumber) {
        String cacheKey = CACHE_KEY_PREFIX + accountNumber;

        return redisTemplate.opsForList().range(cacheKey, 0, -1)
                .cast(Transaction.class)
                .switchIfEmpty(
                        transactionRepository.getTransactionHistory(accountNumber)
                                .collectList()
                                .flatMapMany(transactions -> {
                                    if (transactions.isEmpty()) {
                                        return Flux.empty();
                                    }

                                    return redisTemplate.opsForList().rightPushAll(cacheKey, transactions.toArray())
                                            .then(redisTemplate.expire(cacheKey, CACHE_TTL))
                                            .thenMany(Flux.fromIterable(transactions));
                                })
                )
                .onErrorResume(ex -> {
                    log.error("Error fetching transaction history for account {}: {}", accountNumber, ex.getMessage());
                    return Flux.error(ex);
                });
    }

    @Override
    public Flux<Transaction> getTransactionHistoryUser(){
        return accountClient
                .getMyAccount()
                .flatMapMany(acc->{
                    log.info(">>>Fetching account info for user account number: {} ",acc.getAccountNumber());
                    return getTransactionHistoryByAccountNumber(acc.getAccountNumber());
                });
    }

    @Transactional
    @Override
    public Mono<Void> compensateTransaction(String refNo) {
        log.info("Compensating Amount for ref No: {}", refNo);
        return transactionRepository.findByReferenceNumber(refNo)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid reference number")))
                .flatMap(transaction -> {
                    log.info("Compensating amount: {} to sender: {}", transaction.getAmount(), transaction.getSenderAccountNumber());
                    return refundSender(transaction.getSenderAccountNumber(), transaction.getAmount())
                            .flatMap(res -> {
                                transaction.setStatus(TransactionStatus.FLAGGED);
                                transaction.setNewToFalse();
                                transaction.setCompletedAt(LocalDateTime.now());
                                return transactionRepository.save(transaction)
                                        .flatMap(trans -> {
                                            notifySagaStream(trans); // <--- SSE EVENT EMITTED (FLAGGED)
                                            return evictCaches(trans.getReferenceNumber(),
                                                    trans.getSenderAccountNumber(),
                                                    trans.getReceiverAccountNumber())
                                                    .then(Mono.fromRunnable(() -> transactionProducer.publishRefund(trans)));
                                        });
                            });
                }).then();
    }

    @Transactional
    @Override
    public Mono<Void> completeTransaction(String refNo) {
        log.info("Completing Transaction Amount for ref No: {}", refNo);
        return transactionRepository.findByReferenceNumber(refNo)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid reference number")))
                .flatMap(transaction -> {
                    log.info("Transferring amount: {} to Receiver: {}", transaction.getAmount(), transaction.getReceiverAccountNumber());
                    return refundSender(transaction.getReceiverAccountNumber(), transaction.getAmount())
                            .flatMap(res -> {
                                log.info(">>Changing the status of the transaction as completed:");
                                transaction.setStatus(TransactionStatus.COMPLETED);
                                transaction.setNewToFalse();
                                transaction.setCompletedAt(LocalDateTime.now());
                                return transactionRepository.save(transaction)
                                        .flatMap(trans -> {
                                            notifySagaStream(trans); // <--- SSE EVENT EMITTED (COMPLETED)
                                            return evictCaches(trans.getReferenceNumber(),
                                                    trans.getSenderAccountNumber(),
                                                    trans.getReceiverAccountNumber())
                                                    .then(Mono.fromRunnable(() -> transactionProducer.publishCompletion(trans)));
                                        });
                            });
                }).then(Mono.fromRunnable(()->log.info("The transaction has been saved "))).then();
    }

    private Mono<Map<String, String>> refundSender(String accountNumber, BigDecimal amount) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/internal/accounts/{id}/credit")
                        .queryParam("amount", amount)
                        .build(accountNumber))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new ResponseStatusException(
                                        clientResponse.statusCode(), "Compensation/Credit failed: " + errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {});
    }

    private Mono<Void> evictCaches(String refNo, String senderAccount, String receiverAccount) {
        String refKey = "txn:ref:" + refNo;
        String senderHistoryKey = CACHE_KEY_PREFIX + senderAccount;
        String receiverHistoryKey = CACHE_KEY_PREFIX + receiverAccount;

        return redisTemplate.delete(refKey, senderHistoryKey, receiverHistoryKey).then();
    }

    /**
     * Helper to broadcast real-time SSE updates to connected frontend clients
     */
    private void notifySagaStream(Transaction tx) {
        if (tx != null && sagaStreamBroadcaster != null) {
            TransferResponseDTO dto = TransferResponseDTO.builder()
                    .id(tx.getId())
                    .senderAccountNumber(tx.getSenderAccountNumber())
                    .receiverAccountNumber(tx.getReceiverAccountNumber())
                    .referenceNumber(tx.getReferenceNumber())
                    .amount(tx.getAmount())
                    .createdAt(tx.getCreatedAt())
                    .completedAt(tx.getCompletedAt())
                    .status(tx.getStatus())
                    .description(tx.getDescription())
                    .failureReason(tx.getFailureReason())
                    .type(tx.getType())
                    .build();

            sagaStreamBroadcaster.emitUpdate(dto);
        }
    }
}
