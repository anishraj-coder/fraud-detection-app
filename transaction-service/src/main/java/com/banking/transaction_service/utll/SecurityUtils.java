package com.banking.transaction_service.utll;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

public class SecurityUtils {

    /**
     * Extracts the Keycloak 'sub' claim (unique userId).
     */
    public static Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(token -> token.getToken().getSubject());
    }

    /**
     * Extracts the raw Bearer JWT token string to forward in downstream WebClient calls.
     */
    public static Mono<String> getCurrentBearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(token -> token.getToken().getTokenValue());
    }
}