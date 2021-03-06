package com.disposableemail.service.impl;

import com.disposableemail.exception.AccountAlreadyRegisteredException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.ws.rs.core.Response;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationService implements AuthorizationService {

    @Value("${keycloak.server.realm}")
    private String realm;
    @Value("${keycloak.server.url}")
    private String serverUrl;
    @Value("${keycloak.server.client}")
    private String client;
    private final Keycloak keycloak;
    private final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;

    private final String USERNAME_CLAIM = "preferred_username";

    @Override
    public Response createUser(Credentials credentials) {
        userRepresentation.setUsername(credentials.getAddress());
        credentialRepresentation.setValue(credentials.getPassword());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatusInfo().equals(Response.Status.CONFLICT)) {
            log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                    response.getStatus(), response.getStatusInfo());
            throw new AccountAlreadyRegisteredException();
        }
        log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                response.getStatus(), response.getStatusInfo());
        return response;
    }

    @Override
    public Token getToken(Credentials credentials) {
        log.info("Keycloak | Getting Token string from a Token Manager");
        Keycloak instance = Keycloak.getInstance(serverUrl, realm, credentials.getAddress(),
                credentials.getPassword(), client);
        var tokenManager = instance.tokenManager();
        return new Token(tokenManager.getAccessTokenString());
    }

    @Override
    public Mono<String> getUserName(ServerWebExchange exchange) {

        log.info("Getting user name from {}}", USERNAME_CLAIM);
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal())
                .cast(Jwt.class)
                .map(jwt -> jwt.getClaimAsString(USERNAME_CLAIM));
    }
}
