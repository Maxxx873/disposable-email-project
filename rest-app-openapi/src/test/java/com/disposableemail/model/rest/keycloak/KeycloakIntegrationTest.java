package com.disposableemail.model.rest.keycloak;

import com.disposableemail.config.TestConfig;
import com.disposableemail.exception.AccountAlreadyRegisteredException;
import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.auth.AuthorizationService;
import com.rabbitmq.client.Channel;
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
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

    @Autowired
    public Channel channel;


    private static final Credentials credentials = new Credentials("test@test.org", "password");
    private static final long amqpDeliveryTag = 1;

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
    void shouldSuccessfullyAddUser() throws ExecutionException, InterruptedException, IOException {
        log.debug("shouldSuccessfullyAddUserTest()");

        var futureKeycloakResponse = authorizationService.createUser(getCredentialsEncrypted(), channel, amqpDeliveryTag);

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);
    }

    @Test
    void shouldConflictAddUserIfUserAlreadyExisting() throws ExecutionException, InterruptedException, IOException {
        log.debug("shouldConflictAddUserIfUserAlreadyExisting()");

        var futureKeycloakResponse = authorizationService.createUser(getCredentialsEncrypted(), channel, amqpDeliveryTag)
                .thenApply(response -> {
                    try {
                        return authorizationService.createUser(getCredentialsEncrypted(), channel, amqpDeliveryTag);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).get();

        assertThatThrownBy(futureKeycloakResponse::get).hasCauseInstanceOf(AccountAlreadyRegisteredException.class);
    }

    @Test
    void shouldSuccessfullyGetToken() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        log.debug("shouldSuccessfullyGetToken()");

        var token = authorizationService.createUser(getCredentialsEncrypted(), channel, amqpDeliveryTag)
                .thenApply(response -> authorizationService.getToken(credentials)).get();

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

