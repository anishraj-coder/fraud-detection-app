package com.banking.transaction_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CasdoorReactiveJwtAuthenticationConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Flux.fromIterable(authorities);
    }
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract nested roles array from Casdoor JWT
        List<Map<String, Object>> roles = jwt.getClaim("roles");

        if (roles != null) {
            for (Map<String, Object> roleMap : roles) {
                String roleName = (String) roleMap.get("name");
                if (roleName != null && !roleName.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority(roleName.startsWith("ROLE_")?
                            roleName.toUpperCase():"ROLE_" + roleName.toUpperCase()));
                }
            }
        }

        // Fallback: If no roles found in token, grant default ROLE_USER
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }

        return authorities;
    }
}
