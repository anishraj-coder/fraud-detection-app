package com.banking.transaction_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@RequiredArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChainConfig(ServerHttpSecurity http){
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter=new ReactiveJwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new CasdoorReactiveJwtAuthenticationConverter());
        return http
                .csrf(csrf->csrf.disable())
                .cors(corsSpec -> corsSpec.disable()) // CORS handling centralized at api-gateway-service to prevent duplicate headers
                .authorizeExchange(exchange->exchange
                        .pathMatchers("/api/v1/transactions/transaction/verify/**", "/transaction/verify/**").permitAll()
                        .pathMatchers("/actuator/**","/public/**","/api/v1/internal/**","**/v3/api-docs").permitAll()
                        .pathMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers("/api/v1/**").hasAnyAuthority("ROLE_CUSTOMER", "ROLE_ADMIN")
                        .anyExchange().authenticated()
                ).oauth2ResourceServer(oauth2->oauth2
                        .jwt(jwt->jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                ).build();
    }
}
