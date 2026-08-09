package com.banking.apigatewayservice.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class RateLimitingConfig {

    /**
     * Resolves the key used for rate-limiting.
     * Strategy: Uses Client IP Address (or falls back to "anonymous").
     */
    @Bean
    public KeyResolver keyResolver(){
        return exchange -> Mono.just(
                Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(address-> address.getAddress().getHostAddress())
                        .orElse("anonymous")
        );
    }
    /**
     * Default Rate Limiter rule:
     * - replenishRate: 10 requests per second (refill rate)
     * - burstCapacity: 20 maximum requests allowed in 1 second
     */
    @Bean
    public RedisRateLimiter redisRateLimiterConfig(){
        return new RedisRateLimiter(10, 20);
    }
}

