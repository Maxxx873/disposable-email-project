package com.disposableemail.apache.james.mailet;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class DockerMongoDB {
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("mongo");
    private static final int DEFAULT_PORT = 27017;
    private final GenericContainer<?> container;

    public DockerMongoDB() {
        this.container = getContainer();
    }

    private GenericContainer<?> getContainer() {
        return new GenericContainer<>(DEFAULT_IMAGE_NAME).withExposedPorts(DEFAULT_PORT);
    }

    public void start() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    public void stop() {
        container.stop();
    }

    public int getMappedPort() {
        return container.getMappedPort(DEFAULT_PORT);
    }
}
