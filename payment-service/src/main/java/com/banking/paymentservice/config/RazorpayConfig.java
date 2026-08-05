package com.banking.paymentservice.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RazorpayConfig {
    public final String razorpayId;
    private final String razorpaySecret;
    private final String webhookSecret;

    public RazorpayConfig(@Value("${razorpay.key-id}") String razorpayId,
                          @Value("${razorpay.key-secret}") String razorpaySecret,
                          @Value("${razorpay.webhook-secret}") String webhookSecret){
        this.razorpayId=razorpayId;
        this.razorpaySecret=razorpaySecret;
        this.webhookSecret=webhookSecret;
    }

    @Bean
    public RazorpayClient razorpay() throws RazorpayException {
        return new RazorpayClient(razorpayId,razorpaySecret);
    }

}
