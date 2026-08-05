package com.banking.transaction_service.config;

import com.banking.transaction_service.utll.SecurityUtils;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class LibraryConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilderConfig() {
        return WebClient.builder();
    }

    @Bean
    public WebClient createWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("lb://ACCOUNT-SERVICE/api/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(forwardCorrelationIdFilter())
                .filter(forwardBearerTokenFilter())
                .build();
    }

    private ExchangeFilterFunction forwardCorrelationIdFilter() {
        return (request, next) -> Mono.deferContextual(ctxView -> {
            String correlationId = ctxView.getOrDefault(TraceLoggingFilter.CORRELATION_HEADER, "UNKNOWN_TRACE");

            ClientRequest mutatedRequest = ClientRequest.from(request)
                    .header(TraceLoggingFilter.CORRELATION_HEADER, correlationId)
                    .build();

            return next.exchange(mutatedRequest);
        });
    }

    private ExchangeFilterFunction forwardBearerTokenFilter() {
        return (request, next) -> SecurityUtils.getCurrentBearerToken()
                .map(token -> ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build())
                .defaultIfEmpty(request)
                .flatMap(next::exchange);
    }
}