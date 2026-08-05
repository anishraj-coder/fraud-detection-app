package com.banking.apigatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter=new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CasdoorReactiveJwtAuthenticationConverter());
        return http
                .csrf(csrf -> csrf.disable())
                .cors(corsSpec -> corsSpec.disable()) // CORS handled centrally by CorsWebFilter Bean
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints for API Docs & Swagger UI
                        .pathMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/v1/accounts/v3/api-docs",
                                "/api/v1/transactions/v3/api-docs",
                                "/api/v1/payments/v3/api-docs"
                        ).permitAll()
                        // Verification  OTP Path
                        .pathMatchers(HttpMethod.GET,"/api/v1/transactions/transaction/verify/**").permitAll()

                        // Public / Webhook Endpoints
                        .pathMatchers("/fallback/**", "/actuator/**", "/public/**", "/api/v1/payments/webhook").permitAll()

                        // Admin Domain
                        .pathMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")

                        // Customer & Transaction Domain
                        .pathMatchers(
                                "/api/v1/accounts/**",
                                "/api/v1/transfer/**",
                                "/api/v1/transaction/**",
                                "/api/v1/transactions/**",
                                "/api/v1/payments/**"
                        ).hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADMIN")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }
}
