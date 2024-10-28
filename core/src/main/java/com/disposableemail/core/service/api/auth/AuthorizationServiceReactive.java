package com.disposableemail.core.service.api.auth;

import com.disposableemail.core.model.Credentials;
import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;

public interface AuthorizationServiceReactive {
    Mono<Response> createUser(Credentials credentials);
}
