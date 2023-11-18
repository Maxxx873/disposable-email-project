package com.disposableemail.core.service.impl.auth;

import com.disposableemail.config.TestConfig;
import com.disposableemail.core.exception.custom.AccountAlreadyRegisteredException;
import com.disposableemail.core.exception.custom.AccountNotFoundException;
import com.disposableemail.core.model.Credentials;
import com.disposableemail.core.service.api.auth.AuthorizationService;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Slf4j
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private final static String REALM = "master";
    private final static String USER_ROLE = "USER";

    @BeforeAll
    void addRealmRole() {
        var roleRep = new RoleRepresentation();
        roleRep.setName(USER_ROLE);
        roleRep.setDescription("role_" + USER_ROLE);
        keycloak.realm(REALM).roles().create(roleRep);
    }

    @AfterEach
    void clearUsers() {
        getRealms().forEach(realmRepresentation ->
                getUsers(realmRepresentation).forEach(user -> {
                            var realm = realmRepresentation.getRealm();
                            if (user.getUsername().equals(credentials.getAddress())) {
                                keycloak.realm(realm).users().delete(user.getId());
                            }
                        }
                )
        );
    }

    @Test
    void shouldSuccessfullyAddUser() throws ExecutionException, InterruptedException {
        log.debug("shouldSuccessfullyAddUserTest()");

        var futureKeycloakResponse = authorizationService.createUser(getCredentialsEncrypted());

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.CREATED);
    }

    @Test
    void shouldSuccessfullyAddUserWithRoleUser() {
        log.debug("shouldSuccessfullyAddUserWithRoleUserTest()");

        authorizationService.createUser(getCredentialsEncrypted());

        var userId = keycloak.realm(REALM).users().search(credentials.getAddress()).get(0).getId();
        var roleName = keycloak.realm(REALM).roles().get(USER_ROLE).toRepresentation().getName();
        var user = keycloak.realm(REALM).users().get(userId);
        var userRoles = user.roles().realmLevel().listAll();

        assertThat(userRoles.stream().map(RoleRepresentation::getName).toList()).asList().contains(roleName);
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

        var futureKeycloakResponse = authorizationService.deleteUserByName(credentials.getAddress());

        assertThat(futureKeycloakResponse.get().getStatusInfo()).isEqualTo(Response.Status.NO_CONTENT);
    }

    @Test
    void shouldThrowAccountNotFoundDeleteNonExistentUser() throws ExecutionException, InterruptedException {
        log.debug("shouldNotFoundDeleteNonExistentUser()");

        authorizationService.createUser(getCredentialsEncrypted());

        assertThatThrownBy(() -> authorizationService.deleteUserByName("nonExistentName"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("This Account not found");
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

