package com.banking.paymentservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceLoggingFilter implements WebFilter {
    public static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        String correlationId;

        if (headers == null || !headers.containsHeader(CORRELATION_HEADER)) {
            correlationId = UUID.randomUUID().toString();
            log.warn("No correlation ID found in incoming request; generated fallback: {}", correlationId);
        } else {
            correlationId = headers.getFirst(CORRELATION_HEADER);
            log.info("Preserved incoming Correlation ID: {}", correlationId);
        }

        exchange.getResponse().getHeaders().add(CORRELATION_HEADER, correlationId);

        return chain.filter(exchange)
                .contextWrite(Context.of(CORRELATION_HEADER, correlationId));
    }
}