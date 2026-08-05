package com.banking.frauddetectionservice.config;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LibConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilderConfig(){
        return WebClient.builder();
    }

    @Bean
    public WebClient webClientConfig(WebClient.Builder builder ){
        return builder
                .baseUrl("lb://ACCOUNT-SERVICE/api/v1/internal/accounts")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
    }
}
