package com.disposableemail.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    private final JwtDecoderConfiguration decoderConfiguration;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.password}")
    private String password;

    @Value("${spring.security.salt}")
    private String salt;


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/**").permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt().jwtDecoder(jwtDecoder());
        return http.build();
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        return decoderConfiguration.getReactiveJwtDecoder(issuerUri);
    }

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.text(password, salt);
    }
}
