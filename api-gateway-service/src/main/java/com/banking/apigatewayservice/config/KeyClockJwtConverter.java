package com.banking.apigatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KeyClockJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        log.info("Entering for JWT parsing");
        Collection<GrantedAuthority> authorities=convertRoles(jwt);
        log.info("The roles: {}",authorities.stream()
                .map(au->au.getAuthority()).collect(Collectors.joining(",")));
        String username=jwt.getClaim("preferred_username");
        if(username==null){
            username=jwt.getSubject();
        }
        log.info("Username: {}",username);
        return Mono.just(new JwtAuthenticationToken(jwt,authorities,username));
    }

    private Collection<GrantedAuthority> convertRoles(Jwt jwt){
        Set<GrantedAuthority> authoritySet=new HashSet<>();
        Map<String,Object> realm_access=jwt.getClaim("realm_access");
        if(realm_access!=null){
            List<String> roles=(List<String>) realm_access.get("roles");
            authoritySet.addAll(
                    roles.stream()
                            .map(role->new SimpleGrantedAuthority(role.startsWith("ROLE_")?role:"ROLE_"+role))
                            .toList()
            );
        }
        List<String> scopes = jwt.getClaimAsStringList("scope");
        if (scopes != null) {
            authoritySet.addAll(
                    scopes.stream()
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .toList()
            );
        }
        return authoritySet;
    }
}
