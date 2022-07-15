package com.disposableemail.model.rest.keycloak;

import com.disposableemail.rest.model.Credentials;
import com.disposableemail.service.api.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KeycloakIntegrationTest {

    @Autowired
    public AuthorizationService authorizationService;

    @Container
    public static GenericContainer<?> keycloakContainer = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:18.0.1"))
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withEnv("KC_DB", "dev-mem")
            .withCommand("start-dev")
            .waitingFor(Wait.forHttp("/admin").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        keycloakContainer.start();
        String keycloakHost = keycloakContainer.getHost();
        Integer keycloakPort = keycloakContainer.getMappedPort(8080);
        String keycloakServerUrl = String.format("http://%s:%s", keycloakHost, keycloakPort);
        registry.add("keycloak.server.url", () -> keycloakServerUrl);
        registry.add("keycloak.server.password", () -> "admin");
    }

    @Test
    void shouldSuccessfullyAddUserTest() {
        log.debug("shouldSuccessfullyAddUserTest()");
        var credentials = new Credentials("test@test", "password");

        authorizationService.createUser(credentials);

        assertThat(authorizationService.createUser(credentials)).isEqualTo(credentials.getAddress());
    }


}


