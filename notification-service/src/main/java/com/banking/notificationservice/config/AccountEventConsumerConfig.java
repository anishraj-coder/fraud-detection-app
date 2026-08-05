package com.banking.notificationservice.config;

import com.banking.notificationservice.dto.AccountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AccountEventConsumerConfig {

    private final AccountEventHandler accountEventHandler;

    @Bean(name = "accountEventConsumer")
    public Consumer<Message<AccountEvent>> accountEventConsumer() {
        return accountEventMessage -> {
            AccountEvent event = accountEventMessage.getPayload();

            Acknowledgment ack = accountEventMessage.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            if (ack != null) {
                log.info(">>>Acknowledging the account creation message consumption");
                ack.acknowledge();
            }

            log.info(">>Notification Service: successfully Consumed the event {}", event.account().getAccountNumber());
            accountEventHandler.handleAccountEvent(event);
        };
    }
}