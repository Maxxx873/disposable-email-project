package com.disposableemail.model.rest.keycloak;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Testcontainers
public abstract class AbstractKeycloakTestContainer {

    @Container
    private static final GenericContainer<?> keycloakContainer = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:20.0.1"))
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withEnv("KC_DB", "dev-mem")
            .withCommand("start-dev")
            .waitingFor(Wait.forHttp("/admin").forPort(8080).withStartupTimeout(Duration.ofMinutes(1)));

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        keycloakContainer.start();
        String keycloakHost = keycloakContainer.getHost();
        Integer keycloakPort = keycloakContainer.getMappedPort(8080);
        String keycloakServerUrl = String.format("http://%s:%s", keycloakHost, keycloakPort);
        String keycloakAdminPassword;
        keycloakAdminPassword = keycloakContainer.getEnvMap().get("KEYCLOAK_ADMIN_PASSWORD");
        registry.add("keycloak.server.url", () -> keycloakServerUrl);
        registry.add("keycloak.server.password", () -> keycloakAdminPassword);
    }

}
