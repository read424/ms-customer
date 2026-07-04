package com.bootcamp.ms_customer.infrastructure.test.persistence.containers;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoTestContainer {

    private static MongoDBContainer container;

    static {
        container = new MongoDBContainer(
                DockerImageName.parse("mongo:8.3.4")
        );
        container.start();
    }

    public static MongoDBContainer getInstance() {
        return container;
    }

    public static String getConnectionString() {
        return container.getReplicaSetUrl();
    }

    private MongoTestContainer() {
    }
}
