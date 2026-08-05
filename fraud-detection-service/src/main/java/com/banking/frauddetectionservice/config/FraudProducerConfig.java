package com.banking.frauddetectionservice.config;


import com.banking.frauddetectionservice.DTO.FraudCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FraudProducerConfig {

    private final Sinks.Many<Message<FraudCheckResult>>  cleanTransaction=Sinks
            .many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Message<FraudCheckResult>> verificationRequired=Sinks
            .many().multicast().onBackpressureBuffer();


    @Bean(name = "cleanTransactionProducer")
    public Supplier<Flux<Message<FraudCheckResult>>> cleanTransactionProducer(){
        return cleanTransaction::asFlux;
    }

    @Bean(name ="verifyTransactionProducer" )
    public Supplier<Flux<Message<FraudCheckResult>>> verifyTransactionProducer(){
        return verificationRequired::asFlux;
    }

    /**
     * Used to produce the event that the transaction is clean
     * @param check
     */
    public void publishCleanTransaction(FraudCheckResult check){

        log.info("Publishing event of clean Transaction Ref No: {}",check.referenceId());
        Message<FraudCheckResult> message= MessageBuilder.withPayload(check)
                .setHeader("partitionId",check.referenceId())
                .build();
        cleanTransaction.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * Used to produce the event that verification is required for this transaction
     * @param check
     */
    public void publishVerificationRequired(FraudCheckResult check){
        log.info("Publishing event of Fraud Transaction Ref No: {}",check.referenceId());
        Message<FraudCheckResult> message= MessageBuilder.withPayload(check)
                .setHeader("partitionId",check.referenceId())
                .build();
        verificationRequired.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
