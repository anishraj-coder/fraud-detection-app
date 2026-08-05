package com.banking.transaction_service.repository;

import com.banking.transaction_service.entity.Transaction;
import com.banking.transaction_service.entity.enums.TransactionStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction,String> {
    Mono<Transaction> findByReferenceNumber(String referenceNumber);
    @Query("""
            Select * from transactions_fraud t where t.sender_account_number = :accountNumber
             OR t.receiver_account_number = :accountNumber order by t.created_at desc
            """)
    Flux<Transaction> getTransactionHistory(@Param("accountNumber") String accountNumber);

    Flux<Transaction> findByStatusAndCreatedAtBefore(TransactionStatus status, LocalDateTime time);
}
