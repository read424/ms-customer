package com.bootcamp.ms_customer.application.ports.output;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepositoryPort {

    Mono<Customer> storeCustomer(Customer customer);

    Mono<Customer> getCustomerById(String customerId);

    Flux<Customer> getAllCustomers();

    Mono<Void> removeCustomerById(String customerId);

    Mono<Boolean> hasCustomerWithDocumentNumber(String documentNumber);

    Mono<PaginatedResult<Customer>> getCustomersPaginated(int page, int size, CustomerType type);
}
