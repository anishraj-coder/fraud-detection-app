package com.banking.apigatewayservice.routes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestTraceFiler implements GlobalFilter, Ordered {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        String correlationId;

        if (requestHeaders.containsHeader(CORRELATION_HEADER)) {
            correlationId = requestHeaders.getFirst(CORRELATION_HEADER);
            log.info("Existing Correlation ID found in request: {}", correlationId);
        } else {
            correlationId = UUID.randomUUID().toString();
            log.info("Didn't receive the Correlation ID, generated correlation ID: {}", correlationId);
        }

        // 1. Mutate incoming request header to forward down to downstream microservices
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_HEADER, correlationId)
                .build();

        // 2. Add header to response BEFORE passing downstream (while headers are still mutable)
        exchange.getResponse().getHeaders().add(CORRELATION_HEADER, correlationId);

        // 3. Pass the mutated request down the filter chain
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}