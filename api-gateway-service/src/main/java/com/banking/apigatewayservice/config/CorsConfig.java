package com.banking.apigatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class CorsConfig {

    private final String allowedOrigins;

    public CorsConfig(@Value("${security.config.allowed-origins}")String allowedOrigins){
        this.allowedOrigins=allowedOrigins;
    }

    @Bean
    public CorsWebFilter corsWebFilterConfig() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Use allowedOriginPatterns instead of setAllowedOrigins("*") when allowCredentials is true
        log.info(">>Setting allowed origins to : {}",allowedOrigins);
        corsConfig.setAllowedOriginPatterns(Arrays.stream(allowedOrigins.split(",")).toList());
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setExposedHeaders(List.of("X-Correlation-ID", "Authorization", "X-Razorpay-Signature"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
}