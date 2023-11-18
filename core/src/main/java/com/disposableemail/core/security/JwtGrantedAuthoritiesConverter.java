package com.disposableemail.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Flux<GrantedAuthority>> {
    private final JwtAuthConverterProperties properties;

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        var realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Map.of());
        var roles = (List<String>) realmAccess.getOrDefault("roles", List.of());
        return Flux.fromStream(roles.stream())
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new);
    }
}
