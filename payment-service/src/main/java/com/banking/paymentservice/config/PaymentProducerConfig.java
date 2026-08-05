package com.banking.paymentservice.config;

import com.banking.paymentservice.DTO.PaymentCompletedEvent;
import com.banking.paymentservice.util.TracerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PaymentProducerConfig {

    private final Sinks.Many<Message<PaymentCompletedEvent>> paymentCompeteSink=Sinks.many()
            .multicast().onBackpressureBuffer();

    @Bean(name = "paymentCompletedProducer")
    public Supplier<Flux<Message<PaymentCompletedEvent>>> paymentCompletedProducer(){
        return paymentCompeteSink::asFlux;
    }

    public Mono<Void> publishPaymentCompleted(PaymentCompletedEvent event) {
        return TracerUtils.getTraceId()
                .doOnNext(traceId -> {
                    log.info(">>> Publishing PaymentCompletedEvent to Kafka for Account: {}, Amount: {}",
                            event.accountNumber(), event.amount());

                    Message<PaymentCompletedEvent> message = MessageBuilder.withPayload(event)
                            .setHeader("partitionKey", event.accountNumber())
                            .setHeader(TraceLoggingFilter.CORRELATION_HEADER, traceId)
                            .build();

                    paymentCompeteSink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
                })
                .then();
    }

}
