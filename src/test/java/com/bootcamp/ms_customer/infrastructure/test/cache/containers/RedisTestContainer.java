package com.bootcamp.ms_customer.infrastructure.test.cache.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class RedisTestContainer {

    private static GenericContainer<?> container;

    static {
        container = new GenericContainer<>(
                DockerImageName.parse("redis:7-alpine")
        )
                .withExposedPorts(6379);
        container.start();
    }

    public static GenericContainer<?> getInstance() {
        return container;
    }

    public static String getConnectionString() {
        return "redis://" + container.getHost() + ":" + container.getFirstMappedPort();
    }

    public static String getHost() {
        return container.getHost();
    }

    public static Integer getPort() {
        return container.getFirstMappedPort();
    }

    private RedisTestContainer() {
    }
}
