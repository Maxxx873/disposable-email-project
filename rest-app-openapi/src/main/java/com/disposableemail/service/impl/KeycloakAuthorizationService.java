package com.disposableemail.service.impl;

import com.disposableemail.rest.model.Credentials;
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

    private final Keycloak keycloak;
    private  final UserRepresentation userRepresentation;
    private final CredentialRepresentation credentialRepresentation;

    @Override
    public String createUser(Credentials credentials) {
        credentialRepresentation.setValue(credentials.getPassword());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        Response response = keycloak.realm(realm).users().create(userRepresentation);
        log.info("Response |  Status: {} | Status Info: {}", response.getStatus(), response.getStatusInfo());
        log.info("Keycloak |  User: {} | has been created", userRepresentation.getUsername());
        return userRepresentation.getUsername();
    }

    @Override
    public String getToken(Credentials credentials) {
        return null;
    }
}
