package com.bootcamp.ms_customer.application.ports.output;

import com.bootcamp.ms_customer.domain.model.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {

    Mono<Customer> save(Customer customer);

    Mono<Customer> findById(String customerId);

    Flux<Customer> findAll();

    Mono<Void> deleteById(String customerId);

    Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
