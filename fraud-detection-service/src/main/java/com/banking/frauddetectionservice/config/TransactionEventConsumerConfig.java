package com.banking.frauddetectionservice.config;

import com.banking.frauddetectionservice.DTO.TransactionInitiated;
import com.banking.frauddetectionservice.service.FraudDetectionService;
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
public class TransactionEventConsumerConfig {

    private final FraudDetectionService fraudDetectionService;

    @Bean(name = "transactionInitConsumer")
    public Function<Flux<Message<TransactionInitiated>>, Mono<Void>> transactionInitConsumer(){
        return transactionInitFlux->transactionInitFlux
                .flatMap(transactionInitiatedMessage -> {
                    TransactionInitiated initDto=transactionInitiatedMessage.getPayload();
                    Acknowledgment ack=transactionInitiatedMessage.getHeaders()
                            .get(KafkaHeaders.ACKNOWLEDGMENT,Acknowledgment.class);
                    if(ack!=null){
                        log.info("Ack of the Message initiation event consumption");
                        ack.acknowledge();;
                    }
                    return fraudDetectionService
                            .fraudCheckResult(initDto);
                }).then();
    }
}
