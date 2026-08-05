package com.banking.transaction_service.config;

import com.banking.transaction_service.DTO.TransactionCompleted;
import com.banking.transaction_service.DTO.TransactionInitiated;
import com.banking.transaction_service.DTO.TransactionRefunded;
import com.banking.transaction_service.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Slf4j
@Configuration
public class TransactionProducerConfig {

    private final Sinks.Many<Message<TransactionInitiated>> initiatorSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Message<TransactionCompleted>> completionSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Message<TransactionRefunded>> refundedSink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean("transactionInitiatorProducer")
    public Supplier<Flux<Message<TransactionInitiated>>> transactionInitiatorProducer() {
        return initiatorSink::asFlux;
    }

    @Bean("transactionCompletedProducer")
    public Supplier<Flux<Message<TransactionCompleted>>> transactionCompletedProducer() {
        return completionSink::asFlux;
    }

    @Bean("transactionRefundedProducer")
    public Supplier<Flux<Message<TransactionRefunded>>> transactionRefundedProducer() {
        return refundedSink::asFlux;
    }

    public void publishInitiation(Transaction transaction) {
        log.info(">>>Publishing event for Transaction Initiation");
        TransactionInitiated transactionInitiated = TransactionInitiated.builder()
                .amount(transaction.getAmount())
                .senderAccountNumber(transaction.getSenderAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
                .referenceId(transaction.getReferenceNumber())
                .build();

        Message<TransactionInitiated> message = MessageBuilder.withPayload(transactionInitiated)
                .setHeader("partitionID",transaction.getReferenceNumber())
                .build();
        initiatorSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public void publishCompletion(Transaction transaction) {
        log.info(">>>Publishing event for Transaction Completion");
        TransactionCompleted transactionCompleted = TransactionCompleted.builder()
                .amount(transaction.getAmount())
                .senderAccountNumber(transaction.getSenderAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
                .referenceId(transaction.getReferenceNumber())
                .build();

        Message<TransactionCompleted> message = MessageBuilder.withPayload(transactionCompleted)
                .setHeader("partitionID",transaction.getReferenceNumber())
                .build();
        completionSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    public void publishRefund(Transaction transaction) {
        log.info(">>>Publishing event for Transaction Refund");
        TransactionRefunded transactionRefunded = TransactionRefunded.builder()
                .amount(transaction.getAmount())
                .senderAccountNumber(transaction.getSenderAccountNumber())
                .receiverAccountNumber(transaction.getReceiverAccountNumber())
                .referenceId(transaction.getReferenceNumber())
                .build();

        Message<TransactionRefunded> message = MessageBuilder.withPayload(transactionRefunded)
                .setHeader("partitionID",transaction.getReferenceNumber())
                .build();
        refundedSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}