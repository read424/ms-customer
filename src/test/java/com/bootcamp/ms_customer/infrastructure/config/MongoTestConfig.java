package com.bootcamp.ms_customer.infrastructure.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class MongoTestConfig {

    @Bean
    public static MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:8.3.4"))
                .withExposedPorts(27017);
        container.start();
        System.setProperty("spring.data.mongodb.uri", container.getReplicaSetUrl("testdb"));
        return container;
    }
}
