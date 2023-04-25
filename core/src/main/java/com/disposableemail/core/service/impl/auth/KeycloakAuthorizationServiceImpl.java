package com.disposableemail.core.service.impl.auth;

import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.EventProducer;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.disposableemail.core.event.Event.Type.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthorizationServiceImpl implements AuthorizationService {

    @Value("${keycloak.server.realm}")
    private String realm;
    @Value("${keycloak.server.url}")
    private String serverUrl;
    @Value("${keycloak.server.client}")
    private String client;
    private final Keycloak keycloak;
    private final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;
    private final EventProducer eventProducer;
    private final TextEncryptor encryptor;

    @Override
    public CompletableFuture<Response> createUser(Credentials credentials) {
        userRepresentation.setUsername(credentials.getAddress());
        credentialRepresentation.setValue(encryptor.decrypt(credentials.getPassword()));
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        return CompletableFuture.completedFuture(getKeycloakCreateUserResponse(credentials));
    }

    //TODO delete

    public CompletableFuture<Response> deleteUser(String username) {
        userRepresentation.setUsername(username);
        return CompletableFuture.completedFuture(getKeycloakDeleteUserResponse(username));
    }

    @Override
    public Token getToken(Credentials credentials) {
        log.info("Keycloak | Getting Token string from a Token Manager for {}", credentials.getAddress());
        var instance = Keycloak.getInstance(serverUrl, realm, credentials.getAddress(),
                credentials.getPassword(), client);
        var tokenManager = instance.tokenManager();
        return new Token(tokenManager.getAccessTokenString());
    }

    private Response getKeycloakCreateUserResponse(Credentials credentials) {
        var response = keycloak.realm(realm).users().create(userRepresentation);
        if (response.getStatusInfo().equals(Response.Status.CONFLICT)) {
            log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                    response.getStatus(), response.getStatusInfo());
            throw new AccountAlreadyRegisteredException();
        }
        if (response.getStatusInfo().equals(Response.Status.CREATED)) {
            eventProducer.send(new Event<>(KEYCLOAK_REGISTER_CONFIRMATION, credentials));
        }
        log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                response.getStatus(), response.getStatusInfo());
        return response;
    }

    //TODO delete
    private Response getKeycloakDeleteUserResponse(String id) {
        var response = keycloak.realm(realm).users().delete(userRepresentation.getId());
        if (response.getStatusInfo().equals(Response.Status.CONFLICT)) {
            log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                    response.getStatus(), response.getStatusInfo());
            throw new AccountAlreadyRegisteredException();
        }
        if (response.getStatusInfo().equals(Response.Status.CREATED)) {
            eventProducer.send(new Event<>(KEYCLOAK_DELETING_CONFIRMATION, id));
        }
        log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                response.getStatus(), response.getStatusInfo());
        return response;
    }
}