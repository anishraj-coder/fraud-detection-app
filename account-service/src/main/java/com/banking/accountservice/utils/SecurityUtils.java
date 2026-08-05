package com.banking.accountservice.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

public class SecurityUtils {

    public static Mono<String> getCurrentUserId(){
        return ReactiveSecurityContextHolder
                .getContext()
                .map(s->s.getAuthentication())
                .filter(auth->auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuthenticationToken -> jwtAuthenticationToken.getToken())
                .map(jwt -> jwt.getSubject());
    }

    public static Mono<String> getCurrentEmail(){
        return ReactiveSecurityContextHolder
                .getContext()
                .map(s->s.getAuthentication())
                .filter(auth->auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuthenticationToken -> jwtAuthenticationToken.getToken())
                .map(jwt->jwt.getClaimAsString("email"));
    }
}
