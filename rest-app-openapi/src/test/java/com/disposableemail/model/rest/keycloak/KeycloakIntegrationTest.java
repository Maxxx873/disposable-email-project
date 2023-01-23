package com.disposableemail.model.rest.keycloak;

import com.disposableemail.config.TestConfig;
import com.disposableemail.exception.AccountAlreadyRegisteredException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;
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
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

@Slf4j
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
    void shouldSuccessfullyAddUser() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyAddUserTest()");

        var future = authorizationService.createUser(getCredentialsEncrypted());

        assertThat(future.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);

    }

    @Test
    void shouldConflictAddUserIfUserAlreadyExisting() throws ExecutionException, InterruptedException {
        log.debug("shouldConflictAddUserIfUserAlreadyExisting()");

        var future1 = authorizationService.createUser(getCredentialsEncrypted());
        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);


        assertThat(future1.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);

        var future2 = authorizationService.createUser(getCredentialsEncrypted());
        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        assertThat(future2.isCompletedExceptionally()).isTrue();
        assertThatThrownBy(future2::get).hasCauseInstanceOf(AccountAlreadyRegisteredException.class);
    }

    @Test
    void shouldSuccessfullyGetToken() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyGetToken()");

        var future = authorizationService.createUser(getCredentialsEncrypted());
        await().pollDelay(Durations.TWO_SECONDS).until(() -> true);

        assertThat(future.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);

        var token = authorizationService.getToken(credentials);

        assertThat(token).isNotNull();
    }

    @Test
    void shouldFailedGetTokenIfAccountNotRegistered() {
        log.debug("shouldFailedGetTokenIfAccountNotRegistered()");

        assertThatThrownBy(() -> authorizationService.getToken(credentials))
                .isInstanceOf(NotAuthorizedException.class);
    }

    @NotNull
    private Credentials getCredentialsEncrypted() {
        return new Credentials()
                .address(credentials.getAddress())
                .password(encryptor.encrypt(credentials.getPassword()));
    }

}


