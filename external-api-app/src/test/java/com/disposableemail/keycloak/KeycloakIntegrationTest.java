package com.disposableemail.keycloak;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Slf4j
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeycloakIntegrationTest extends AbstractKeycloakTestContainer {

    @Autowired
    public Keycloak keycloak;

    @Autowired
    public AuthorizationService authorizationService;

    @Autowired
    public TextEncryptor encryptor;

    private static final Credentials credentials = new Credentials("test@test.org", "password");

    @AfterEach
    void clearUsers() {
        getRealms().forEach(realmRepresentation -> {
            getUsers(realmRepresentation).forEach(user -> {
                var realm = realmRepresentation.getRealm();
                if (user.getUsername().equals(credentials.getAddress())) {
                    keycloak.realm(realm).users().delete(user.getId());
                }
            });
        });
    }

    @Test
    void shouldSuccessfullyAddUser() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyAddUserTest()");

        var futureKeycloakResponse = authorizationService.createUser(getCredentialsEncrypted());

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);
    }

    @Test
    void shouldConflictAddUserIfUserAlreadyExisting() {
        log.debug("shouldConflictAddUserIfUserAlreadyExisting()");

        authorizationService.createUser(getCredentialsEncrypted());

        assertThatThrownBy(() -> assertThat(authorizationService.createUser(getCredentialsEncrypted()))
                .isCompletedExceptionally())
                .isInstanceOf(AccountAlreadyRegisteredException.class);
    }

    @Test
    void shouldSuccessfullyGetToken() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyGetToken()");

        var token = authorizationService.createUser(getCredentialsEncrypted())
                .thenApply(response -> authorizationService.getToken(credentials)).get();

        assertThat(token).isNotNull();
    }

    @Test
    void shouldFailedGetTokenIfAccountNotRegistered() {
        log.debug("shouldFailedGetTokenIfAccountNotRegistered()");

        assertThatThrownBy(() -> authorizationService.getToken(credentials))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @Test
    void shouldSuccessfullyDeleteUser() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyDeleteUser()");

        authorizationService.createUser(getCredentialsEncrypted());

        var futureKeycloakResponse = authorizationService.deleteUser(getUserId());

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.NO_CONTENT);
    }

    @Test
    void shouldNotFoundDeleteNonExistentUser() throws ExecutionException, InterruptedException {
        log.debug("shouldNotFoundDeleteNonExistentUser()");

        authorizationService.createUser(getCredentialsEncrypted());

        var futureKeycloakResponse = authorizationService.deleteUser("nonExistentId");

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.NOT_FOUND);
    }

    @NotNull
    private String getUserId() {
        var ref = new Object() {
            String userId = "";
        };
        getRealms().forEach(realmRepresentation -> {
            getUsers(realmRepresentation).forEach(user -> {
                if (user.getUsername().equals(credentials.getAddress())) {
                    ref.userId = user.getId();
                }
            });
        });
        return ref.userId;
    }

    private List<UserRepresentation> getUsers(RealmRepresentation realmRepresentation) {
        return keycloak.realm(realmRepresentation.getRealm()).users().list();
    }

    private List<RealmRepresentation> getRealms() {
        return keycloak.realms().findAll();
    }

    @NotNull
    private Credentials getCredentialsEncrypted() {
        return new Credentials()
                .address(credentials.getAddress())
                .password(encryptor.encrypt(credentials.getPassword()));
    }

}

