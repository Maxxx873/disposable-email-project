package com.disposableemail.core.service.impl.auth;

import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.auth.AuthorizationServiceReactive;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationServiceReactiveImpl implements AuthorizationServiceReactive {

    private final KeycloakAdminUtils keycloakAdminUtils;

    @Override
    public Mono<Response> createUser(Credentials credentials) {
        return Mono.fromCallable(() -> keycloakAdminUtils.getKeycloakCreateUserResponse(credentials))
                .onErrorResume(Mono::error)
                .subscribeOn(Schedulers.boundedElastic());
    }

}
