package com.disposableemail.core.service.impl.auth;

import com.disposableemail.core.event.Event;
import com.disposableemail.core.event.producer.EventProducer;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.model.Token;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.disposableemail.core.event.Event.Type.AUTH_REGISTER_CONFIRMATION;
import static com.disposableemail.core.event.Event.Type.DELETING_ACCOUNT;


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
    @Value("${spring.security.role.user}")
    private String userRoleName;

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

    @Override
    public CompletableFuture<Response> deleteUserByName(String username) {
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
        try {
            var response = keycloak.realm(realm).users().create(userRepresentation);
            if (response.getStatusInfo().equals(Response.Status.CONFLICT)) {
                log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                        response.getStatus(), response.getStatusInfo());
                throw new AccountAlreadyRegisteredException();
            }
            if (response.getStatusInfo().equals(Response.Status.FORBIDDEN)) {
                eventProducer.send(new Event<>(DELETING_ACCOUNT, credentials.getAddress()));
                log.error("Keycloak |  User: {} | Status: {} | Status Info: {}", credentials.getAddress(),
                        response.getStatus(), response.getStatusInfo());
            }
            if (response.getStatusInfo().equals(Response.Status.CREATED)) {
                addRealmRoleToUser(credentials.getAddress(), userRoleName);
                eventProducer.send(new Event<>(AUTH_REGISTER_CONFIRMATION, credentials));
            }
            log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                    response.getStatus(), response.getStatusInfo());
            return response;
        } catch (ProcessingException e) {
            eventProducer.send(new Event<>(DELETING_ACCOUNT, credentials.getAddress()));
            log.error("Keycloak error | {}", e.getMessage());
        }
        return Response.noContent().build();
    }

    private Response getKeycloakDeleteUserResponse(String username) {
        var userId = getKeycloakUserIdByName(username);
        if (userId.isEmpty()) {
            log.error("Keycloak not found username | {}", username);
            return Response.notModified().build();
        } else {
            var response = keycloak.realm(realm).users().delete(userId.get());
            log.info("Keycloak | Deleting user: {} | Status: {} | Status Info: {}", username,
                    response.getStatus(), response.getStatusInfo());
            return response;
        }
    }

    private Optional<String> getKeycloakUserIdByName(String name) {
        try {
            var representation = keycloak.realm(realm).users().search(name, true)
                    .stream()
                    .findFirst();
            return representation.map(UserRepresentation::getId);
        } catch (ProcessingException e) {
            log.error("Keycloak error | {}", e.getMessage());
        }
        return Optional.empty();
    }

    private void addRealmRoleToUser(String username, String userRoleName) {
        var userId = getKeycloakUserIdByName(username);
        userId.ifPresent(id -> {
            var user = keycloak.realm(realm).users().get(id);
            if (isRoleContains(userRoleName)) {
                List<RoleRepresentation> roleToAdd = new ArrayList<>();
                roleToAdd.add(keycloak.realm(realm).roles().get(userRoleName).toRepresentation());
                user.roles().realmLevel().add(roleToAdd);
                log.info("Keycloak |  User: {} | Added role: {}", userRepresentation.getUsername(), userRoleName);
            }
        });
    }

    private boolean isRoleContains(String userRoleName) {
        return keycloak.realm(realm).roles().list().stream()
                .map(RoleRepresentation::getName)
                .toList()
                .contains(userRoleName);
    }
}