package com.disposableemail.core.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public final class SecurityUtils {

    public static Mono<UserCredentials> getCredentialsFromJwt() {

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(UserCredentials::convert);
    }
}
