package com.banking.notificationservice.config;

import com.banking.notificationservice.dto.OtpVerification;
import com.banking.notificationservice.service.EmailService;
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
public class OtpEventConsumerConfig {

    private final EmailService emailService;

    @Bean(name = "otpEventConsumer")
    public Consumer<Message<OtpVerification>> otpEventConsumer() {
        return otpVerificationMessage -> {
            OtpVerification otpVerification = otpVerificationMessage.getPayload();
            log.info(">>Intercepted the message for otp: Ref No: {}, otp: {}, email: {}",
                    otpVerification.referenceId(), otpVerification.otp(), otpVerification.email());

            Acknowledgment ack = otpVerificationMessage.getHeaders()
                    .get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);

            emailService.sendOtpEmail(
                            otpVerification.email(),
                            otpVerification.otp(),
                            otpVerification.amount(),
                            otpVerification.referenceId()
                    )
                    .doOnSuccess(v -> {
                        if (ack != null) {
                            log.info(">>Ack of the consumption");
                            ack.acknowledge();
                        }
                    })
                    .doOnError(e -> log.error(">>Failed to process OTP event: {}", e.getMessage()))
                    .subscribe();
        };
    }
}