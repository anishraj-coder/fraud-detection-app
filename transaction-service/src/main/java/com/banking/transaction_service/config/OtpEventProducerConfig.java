package com.banking.transaction_service.config;

import com.banking.transaction_service.DTO.OtpVerification;
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
public class OtpEventProducerConfig {

    private final Sinks.Many<Message<OtpVerification>> sink=Sinks.many().multicast().onBackpressureBuffer();

    @Bean(name = "otpVerificationProducer")
    public Supplier<Flux<Message<OtpVerification>>> otpVerificationProducer(){
        return sink::asFlux;
    }

    public void publishOtpVerificationEvent(OtpVerification event){
        log.info("Publishing the event of otp verification");
        Message<OtpVerification> message= MessageBuilder.withPayload(event)
                .setHeader("partitionID",event.referenceId())
                .build();
        sink.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
