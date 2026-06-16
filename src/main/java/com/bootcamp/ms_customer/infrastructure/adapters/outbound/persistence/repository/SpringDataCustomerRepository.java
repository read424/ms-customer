package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository;

import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SpringDataCustomerRepository extends ReactiveMongoRepository<CustomerEntity, String> {

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
