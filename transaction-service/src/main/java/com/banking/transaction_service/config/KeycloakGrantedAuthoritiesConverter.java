package com.banking.transaction_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                    .forEach(authorities::add);
        }

        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes != null) {
            scopes.stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .forEach(authorities::add);
        }

        return Flux.fromIterable(authorities);
    }
}