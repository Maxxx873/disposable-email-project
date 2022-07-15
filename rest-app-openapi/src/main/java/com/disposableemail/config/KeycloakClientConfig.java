package com.disposableemail.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "authorization", name = "service", havingValue = "keycloak", matchIfMissing = true)
public class KeycloakClientConfig {

    @Value("${keycloak.server.realm}")
    private String realm;
    @Value("${keycloak.server.username}")
    private String username;
    @Value("${keycloak.server.password}")
    private String password;
    @Value("${keycloak.server.client}")
    private String client;

    @Value("${keycloak.server.url}")
    private String serverUrl;

    @Bean
    public Keycloak keycloak(){
        return Keycloak.getInstance(serverUrl, realm, username, password, client);
    }

    @Bean
    public UserRepresentation userRepresentation(){
        var userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        return userRepresentation;
    }

    @Bean
    public CredentialRepresentation credentialRepresentation() {
        var credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        return credentialRepresentation;
    }

}
