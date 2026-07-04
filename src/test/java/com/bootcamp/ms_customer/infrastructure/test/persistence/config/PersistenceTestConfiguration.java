package com.bootcamp.ms_customer.infrastructure.test.persistence.config;

import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.CustomerRepositoryAdapter;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper.CustomerPersistenceMapperImpl;

@Configuration
@EnableReactiveMongoRepositories(
        basePackages = "com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository"
)
@Import({
        MongoReactiveAutoConfiguration.class,
        MongoReactiveDataAutoConfiguration.class,
        CustomerRepositoryAdapter.class,
        CustomerPersistenceMapperImpl.class
})
public class PersistenceTestConfiguration {
}
