package com.banking.accountservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        Map<String,Object> realm=jwt.getClaim("realm_access");
        List<String> roles=(List<String>) realm.get("roles");
        Set<GrantedAuthority>authorities=roles.stream()
                .map(role->role.startsWith("ROLE_")?role:"ROLE_"+role)
                .map(role->new SimpleGrantedAuthority(role.toUpperCase()))
                .collect(Collectors.toSet());
        List<String>scope=jwt.getClaimAsStringList("scope");
        if(scope!=null&&!scope.isEmpty()){
            authorities.addAll(
                    scope.stream()
                            .map(s->new SimpleGrantedAuthority("SCOPE_"+s))
                            .collect(Collectors.toSet())
            );
        }
        return Flux.fromIterable(authorities);
    }
}
