package com.banking.apigatewayservice.routes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    private final RedisRateLimiter rateLimiter;
    private final KeyResolver keyResolver;

    @Bean
    public RouteLocator routeConfig(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway Route Locator");
        return builder.routes()
                // --- ACCOUNT SERVICE ROUTES ---
                .route("account-service", r -> r
                        .path("/api/v1/accounts/**", "/api/v1/admin/accounts/**", "/api/v1/accounts/v3/api-docs")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setKeyResolver(keyResolver)
                                        .setRateLimiter(rateLimiter)
                                )
                                // NOTE: Do NOT combine retry + circuitBreaker with a fallbackUri.
                                // Retry commits the response on its last attempt, then circuitBreaker's
                                // forward dispatch fails with UnsupportedOperationException.
                                // CircuitBreaker alone handles resilience via fallback URI.
                                .circuitBreaker(config -> config
                                        .setName("account-fallback")
                                        .setFallbackUri("forward:/fallback/accounts")
                                        .addStatusCode("500")
                                        .addStatusCode("502")
                                        .addStatusCode("503")
                                        .addStatusCode("504")
                                )
                        ).uri("lb://ACCOUNT-SERVICE")
                )

                // --- TRANSACTION SERVICE ROUTES ---
                .route("transaction-service", r -> r
                        .path(
                                "/api/v1/transfer",
                                "/api/v1/transactions/**",
                                "/api/v1/transfer/**",
                                "/api/v1/transaction",
                                "/api/v1/transaction/**",
                                "/api/v1/admin/transactions/**",
                                "/api/v1/transactions/v3/api-docs"
                        )
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setKeyResolver(keyResolver)
                                        .setRateLimiter(rateLimiter)
                                )
                                .circuitBreaker(config -> config
                                        .setName("transactionCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/transactions")
                                        .addStatusCode("500")
                                        .addStatusCode("502")
                                        .addStatusCode("503")
                                        .addStatusCode("504")
                                )
                        )
                        .uri("lb://TRANSACTION-SERVICE")
                )

                // --- PAYMENT SERVICE ROUTES ---
                .route("payments-service", r -> r
                        .path("/api/v1/payments/**", "/api/v1/payments/v3/api-docs")
                        .filters(f -> f
                                .requestRateLimiter(config -> config
                                        .setKeyResolver(keyResolver)
                                        .setRateLimiter(rateLimiter)
                                )
                                .circuitBreaker(config -> config
                                        .setName("paymentCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payments")
                                        .addStatusCode("500")
                                        .addStatusCode("502")
                                        .addStatusCode("503")
                                        .addStatusCode("504")
                                )
                        )
                        .uri("lb://PAYMENT-SERVICE")
                )
                .build();
    }
}
