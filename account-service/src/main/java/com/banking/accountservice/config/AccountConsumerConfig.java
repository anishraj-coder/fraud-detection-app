package com.banking.accountservice.config;


import com.banking.accountservice.DTO.TransactionEvent;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import com.banking.accountservice.utils.TracerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AccountConsumerConfig {
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    @Bean(name = "transactionInitiationHandler")
    public Function<Flux<Message<TransactionEvent>>, Mono<Void>> transactionInitiationHandler(){
        return transactionFlux->transactionFlux
                .flatMap(transactionMessage->{
                    log.info("Transaction was Initiated and now deduction of amount will happen");
                    TransactionEvent transaction=transactionMessage.getPayload();
                    Acknowledgment ack=transactionMessage.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,Acknowledgment.class);
                    if(ack!=null){
                        log.info("Ack of the transaction Initiation and debit");
                        ack.acknowledge();
                    }
                    String accountNumber=transaction.accountNumber();
                    BigDecimal amount=transaction.amount();
                    return accountService.debitAmount(accountNumber,amount);
                }).doOnError((ex)->log.error("Error occurred while debit from kafka Trace id: {}, ex: {}",
                        TracerUtils.getTraceId(),ex.getMessage()))
                .then();
    }
    @Bean
    public Function<Flux<Message<TransactionEvent>>,Mono<Void>> transactionCreditHandler(){
        return transactionFlux->transactionFlux
                .flatMap(transactionEventMessage -> {
                    TransactionEvent event=transactionEventMessage.getPayload();
                    log.info("Received a request for Credit of amount {} to account: {}",
                            event.amount(),event.accountNumber());
                    Acknowledgment ack=transactionEventMessage.getHeaders()
                            .get(KafkaHeaders.ACKNOWLEDGMENT,Acknowledgment.class);
                    if(ack!=null){
                        log.info("Ack of the  Credit");
                        ack.acknowledge();
                    }
                    String accountNumber= event.accountNumber();
                    BigDecimal amount=event.amount();
                    return accountService.creditAmount(accountNumber,amount);
                }).doOnError((ex)->log.error("Error occurred while Credit from kafka for Trace ID : {}, ex: {}"
                        , TracerUtils.getTraceId(),ex.getMessage()))
                .then();
    }


}
