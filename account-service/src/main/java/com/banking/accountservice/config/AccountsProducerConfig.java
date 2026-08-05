package com.banking.accountservice.config;


import com.banking.accountservice.entity.Account;
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
public class AccountsProducerConfig {

    private Sinks.Many<Message<AccountEvent>> sink=Sinks.many().multicast().onBackpressureBuffer();

    @Bean("accountProducer")
    public Supplier<Flux<Message<AccountEvent>>> accountProducer(){
        return sink::asFlux;
    }

    public void publish(AccountEvent event){
        Message<AccountEvent> message= MessageBuilder.withPayload(event)
                .setHeader("partitionId",event.account().getAccountNumber())
                .build();
        log.info("Producing event of type: {} for account number: {}",event.eventType(),event.account().getAccountNumber());
        sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
