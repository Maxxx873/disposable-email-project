package com.disposableemail.model.rest.keycloak;

import com.disposableemail.config.TestConfig;
import com.disposableemail.exception.AccountAlreadyRegisteredException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Slf4j
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class KeycloakIntegrationTest extends AbstractKeycloakTestContainer {

    @Autowired
    public Keycloak keycloak;

    @Autowired
    public AuthorizationService authorizationService;

    private static final Credentials credentials = new Credentials("test@test.org", "password");

    @AfterEach
    void clearUsers() {
        List<RealmRepresentation> realmReps = keycloak.realms().findAll();
        for (RealmRepresentation realmRep : realmReps) {
            String realm = realmRep.getRealm();
            List<UserRepresentation> userReps = keycloak.realm(realm).users().list();
            userReps.forEach(user -> {
                if (user.getUsername().equals(credentials.getAddress())) {
                    keycloak.realm(realm).users().delete(user.getId());
                }
            });
        }
    }

    @Test
    void shouldSuccessfullyAddUser() {
        log.debug("shouldSuccessfullyAddUserTest()");

        var response = authorizationService.createUser(credentials);

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.CREATED);
    }

    @Test
    void shouldConflictAddUserIfUserAlreadyExisting() {
        log.debug("shouldConflictAddUserIfUserAlreadyExisting()");

        authorizationService.createUser(credentials);

        assertThatThrownBy(() -> authorizationService.createUser(credentials))
                .isInstanceOf(AccountAlreadyRegisteredException.class);
    }

    @Test
    void shouldSuccessfullyGetToken() {
        log.debug("shouldSuccessfullyGetToken()");

        authorizationService.createUser(credentials);
        var token = authorizationService.getToken(credentials);

        assertThat(token.getToken().length()).isPositive();
    }

    @Test
    void shouldFailedGetTokenIfAccountNotRegistered() {
        log.debug("shouldFailedGetTokenIfAccountNotRegistered()");

        assertThatThrownBy(() -> authorizationService.getToken(credentials))
                .isInstanceOf(NotAuthorizedException.class);
    }

}


