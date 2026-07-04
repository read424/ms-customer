package com.bootcamp.ms_customer.infrastructure.test.persistence.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bootcamp.ms_customer.infrastructure.test.persistence.config.PersistenceTestConfiguration;

@Testcontainers
public abstract class AbstractPersistenceAdapterIntegrationTest {

    protected static final AnnotationConfigApplicationContext applicationContext;
    protected ReactiveMongoTemplate mongoTemplate;

    static {
        System.setProperty("spring.data.mongodb.uri", com.bootcamp.ms_customer.infrastructure.test.persistence.containers.MongoTestContainer.getConnectionString());
        applicationContext = new AnnotationConfigApplicationContext(PersistenceTestConfiguration.class);
    }

    @BeforeEach
    void setupDependencies() {
        mongoTemplate = applicationContext.getBean(ReactiveMongoTemplate.class);
    }

    @BeforeEach
    void cleanupDatabase() {
        mongoTemplate.dropCollection("customers").block();
    }

    protected <T> T getBean(Class<T> beanType) {
        return applicationContext.getBean(beanType);
    }
}
