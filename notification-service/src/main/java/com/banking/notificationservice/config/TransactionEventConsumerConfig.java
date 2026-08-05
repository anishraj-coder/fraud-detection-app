package com.banking.notificationservice.config;

import com.banking.notificationservice.dto.transactions.TransactionCompleted;
import com.banking.notificationservice.dto.transactions.TransactionInitiated;
import com.banking.notificationservice.dto.transactions.TransactionRefunded;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class TransactionEventConsumerConfig {

    @Bean(name = "transactionInitConsumer")
    public Consumer<Message<TransactionInitiated>> transactionInitConsumer() {
        return message -> {
            TransactionInitiated initiated = message.getPayload();
            log.info("Intercepted the Event of Initialization of payment: Ref No: {}", initiated.referenceId());

            Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            if (ack != null) {
                log.info(">>>Ack the Init event consumption");
                ack.acknowledge();
            }
        };
    }

    @Bean(name = "transactionRefundConsumer")
    public Consumer<Message<TransactionRefunded>> transactionRefundConsumer() {
        return message -> {
            TransactionRefunded refunded = message.getPayload();
            log.info("Intercepted the Event of Refund of payment: Ref No: {}, Amount: {}",
                    refunded.referenceId(), refunded.amount());

            Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            if (ack != null) {
                log.info(">>>Ack the refund event consumption");
                ack.acknowledge();
            }
        };
    }

    @Bean(name = "transactionCompleteConsumer")
    public Consumer<Message<TransactionCompleted>> transactionCompleteConsumer() {
        return message -> {
            TransactionCompleted completed = message.getPayload();
            log.info("Intercepted the Event of Completion of payment: Ref No: {}", completed.referenceId());

            Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            if (ack != null) {
                log.info(">>>Ack the Completion event consumption");
                ack.acknowledge();
            }
        };
    }
}