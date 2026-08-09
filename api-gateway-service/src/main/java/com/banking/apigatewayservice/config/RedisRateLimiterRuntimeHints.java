package com.banking.apigatewayservice.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class RedisRateLimiterRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("META-INF/scripts/request_rate_limiter.lua");
        hints.resources().registerPattern("META-INF/scripts/*.lua");
    }
}
