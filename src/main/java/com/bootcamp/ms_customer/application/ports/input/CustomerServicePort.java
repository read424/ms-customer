package com.bootcamp.ms_customer.application.ports.input;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerServicePort {

    Mono<Customer> createCustomer(CreateCustomerDto createDto);

    Mono<Customer> findCustomerById(String customerId);

    Flux<Customer> findAllCustomers();

    Mono<Customer> updateCustomer(String customerId, UpdateCustomerDto updateDto);

    Mono<Void> deleteCustomer(String customerId);
}
