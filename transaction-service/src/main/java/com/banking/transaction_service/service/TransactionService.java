package com.banking.transaction_service.service;

import com.banking.transaction_service.DTO.CustomerTransferRequest;
import com.banking.transaction_service.DTO.TransactionRequest;
import com.banking.transaction_service.entity.Transaction;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionService {

    /**
     * Executes transfer for authenticated customers (sender resolved implicitly via token).
     */
    Mono<Transaction> transferMoneyForCustomer(CustomerTransferRequest request);

    /**
     * Executes transfer with explicit sender account number (Admin or internal system calls).
     */
    Mono<Transaction> transferMoney(TransactionRequest request);

    /**
     * Retrieves transaction record by its unique reference number.
     */
    Mono<Transaction> getTransactionByReferenceNumber(String referenceNumber);

    /**
     * Retrieves complete transaction history for a specified account number.
     */
    Flux<Transaction> getTransactionHistoryByAccountNumber(String accountNumber);

    /**
     * Retrieves complete transaction history for current user;
     */
    Flux<Transaction> getTransactionHistoryUser();

    /**
     * Reverts funds to sender during SAGA rollback or fraud failure.
     */
    @Transactional
    Mono<Void> compensateTransaction(String refNo);

    /**
     * Credits receiver account upon successful clean transaction check.
     */
    @Transactional
    Mono<Void> completeTransaction(String refNo);
}