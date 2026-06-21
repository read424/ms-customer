package com.bootcamp.ms_customer.application.ports.input;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageCustomerUseCase {

    Mono<Customer> createCustomer(CreateCustomerDto createDto);

    Mono<Customer> findCustomerById(String customerId);

    Flux<Customer> findAllCustomers();

    Mono<Customer> updateCustomer(String customerId, UpdateCustomerDto updateDto);

    Mono<Void> deleteCustomer(String customerId);

    Mono<PaginatedResult<Customer>> findCustomers(int page, int size, CustomerType type);
}
