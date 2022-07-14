package com.disposableemail.service.impl;

import com.disposableemail.rest.model.Credentials;
import com.disposableemail.rest.model.Token;
import com.disposableemail.service.api.AuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private  final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;

    @Override
    public String createUser(Credentials credentials) {
        userRepresentation.setUsername(credentials.getAddress());
        credentialRepresentation.setValue(credentials.getPassword());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        log.info("Keycloak |  User: {} | Status: {} | Status Info: {}", userRepresentation.getUsername(),
                response.getStatus(), response.getStatusInfo());
        return userRepresentation.getUsername();
    }

    @Override
    public Token getToken(Credentials credentials) {
        log.info("Getting Token string from Keycloak Token Manager");
        Keycloak instance = Keycloak.getInstance(serverUrl, realm, credentials.getAddress(),
                credentials.getPassword(),client);
        var tokenManager = instance.tokenManager();
        var token = new Token();
        token.setToken(tokenManager.getAccessTokenString());
        return token;
    }
}
