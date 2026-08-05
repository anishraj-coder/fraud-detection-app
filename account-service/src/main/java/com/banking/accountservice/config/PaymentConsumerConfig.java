package com.banking.accountservice.config;


import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.repository.AccountRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PaymentConsumerConfig {

    private final AccountRepository accountRepository;
    private final AccountsProducerConfig accountProducerConfig;

    @Bean(name = "paymentCompletedConsumer")
    public Function<Flux<Message<PaymentCompletedEventPayload>>, Mono<Void>> paymentCompletedConsumer() {
        return eventFlux -> eventFlux
                .flatMap(message -> {
                    PaymentCompletedEventPayload event = message.getPayload();
                    log.info(">> Received PaymentCompletedEvent for Account: {}, Amount: {}",
                            event.accountNumber(), event.amount());

                    Acknowledgment ack = message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
                    if (ack != null) {
                        ack.acknowledge();
                    }
                    String traceId = message.getHeaders().get(TraceLoggingFilter.CORRELATION_HEADER, String.class);
                    if (traceId == null) {
                        traceId = "UNKNOWN_TRACE";
                    }
                    log.info(">> [Trace: {}] Received Kafka Event for Account: {}", traceId, event.accountNumber());

                    return accountRepository.findByAccountNumber(event.accountNumber())
                            .flatMap(account -> {
                                account.setNewToFalse();
                                account.setAccountBalance(account.getAccountBalance().add(event.amount()));
                                account.setAccountStatus(AccountStatus.ACTIVE); // Activate the account!

                                log.info(">> Activating account {} with new balance: {}",
                                        account.getAccountNumber(), account.getAccountBalance());

                                return accountRepository.save(account)
                                        .doOnSuccess(acc -> {
                                            AccountEvent accountEvent = AccountEvent.builder()
                                                    .account(acc)
                                                    .eventType(EventType.UNBLOCKED)
                                                    .build();
                                            accountProducerConfig.publish(accountEvent);
                                        });
                            })
                            .switchIfEmpty(Mono.fromRunnable(() -> log.error(">> Account not found for activation: {}", event.accountNumber())))
                            .then();
                })
                .doOnError(ex -> log.error("Error activating account from payment webhook: {}", ex.getMessage()))
                .then();
    }

    @Builder
    public record PaymentCompletedEventPayload(
            String accountNumber,
            BigDecimal amount,
            String razorpayOrderId
    ) {}
}