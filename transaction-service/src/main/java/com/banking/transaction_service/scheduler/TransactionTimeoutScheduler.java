package com.banking.transaction_service.scheduler;

import com.banking.transaction_service.entity.enums.TransactionStatus;
import com.banking.transaction_service.repository.TransactionRepository;
import com.banking.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionTimeoutScheduler {
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    @Scheduled(fixedDelay = 10000)
    public void processExpiredTransactions(){
        LocalDateTime cutOffTime=LocalDateTime.now().minus(Duration.ofMinutes(5));
        transactionRepository.findByStatusAndCreatedAtBefore(TransactionStatus.DEBITED,cutOffTime)
                .flatMap((tx)->{
                    log.warn(">> OTP timeout detected for Ref No: {}. Triggering auto-refund.", tx.getReferenceNumber());
                    return transactionService.compensateTransaction(tx.getReferenceNumber());
                }).doOnError(ex -> log.error("Error during auto-compensation scheduler: {}", ex.getMessage()))
                .subscribe();
    }
}