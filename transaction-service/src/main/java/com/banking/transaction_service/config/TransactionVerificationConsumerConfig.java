package com.banking.transaction_service.config;

import com.banking.transaction_service.DTO.FraudCheckResult;
import com.banking.transaction_service.service.TransactionService;
import com.banking.transaction_service.service.TransactionVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
@Configuration
public class TransactionVerificationConsumerConfig {

    private final TransactionVerificationService transactionVerificationService;
    private final TransactionService transactionService;

    @Bean(name = "verificationConsumer")
    public Function<Flux<Message<FraudCheckResult>>, Mono<Void>> verificationConsumer() {
        return verificationFlux -> verificationFlux
                .flatMap(fraudCheckResultMessage -> {
                    FraudCheckResult result = fraudCheckResultMessage.getPayload();
                    log.warn(">>Intercepting an event of Suspicious Transaction: Ref Id: {}",result.referenceId());
                    Acknowledgment ack = fraudCheckResultMessage.getHeaders()
                            .get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
                    if (ack != null) {
                        log.info("Acknowledging the event of required verification");
                        ack.acknowledge();
                    }
                    log.warn(">>Consuming the event of required verification for ref no: {}", result.referenceId());
                    return transactionVerificationService.initiateVerification(result);
                }).then();
    }

    @Bean("cleanCheckConsumer")
    public Function<Flux<Message<FraudCheckResult>>,Mono<Void>> cleanCheckConsumer(){
        return cleanFlux->cleanFlux
                .flatMap(cleanCheckMessage->{
                    FraudCheckResult result=cleanCheckMessage.getPayload();
                    log.info(">>Intercepting the Event of clean Transaction: Ref ID: {}",result.referenceId());
                    Acknowledgment ack=cleanCheckMessage.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,Acknowledgment.class);
                    if(ack!=null){
                        log.info("Acknowledging the event of Clean Transaction");
                        ack.acknowledge();
                    }
                    return transactionService.completeTransaction(result.referenceId());
                }).then();
    }
}
