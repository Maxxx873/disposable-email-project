package com.disposableemail.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Flux;


@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    private final JwtDecoderConfig decoderConfig;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    SecurityWebFilterChain apiHttpSecurity(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/**"))
                .authorizeExchange(exchanges ->
                        exchanges.anyExchange().permitAll())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        http
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                                    jwt.jwtDecoder(jwtDecoder());
                                    jwt.jwtAuthenticationConverter(authenticationConverter(jwtGrantedAuthoritiesConverter));
                                }
                        )
                );
        return http.build();

    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return decoderConfig.getReactiveJwtDecoder(issuerUri);
    }

    @Bean
    public ReactiveJwtAuthenticationConverter authenticationConverter(Converter<Jwt, Flux<GrantedAuthority>> authoritiesConverter) {
        var authenticationConverter = new ReactiveJwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        authenticationConverter.setPrincipalClaimName(StandardClaimNames.PREFERRED_USERNAME);
        return authenticationConverter;
    }

}

