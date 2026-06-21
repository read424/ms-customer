package com.bootcamp.ms_customer.application.service;

import com.bootcamp.ms_customer.application.ports.input.CustomerServicePort;
import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService implements CustomerServicePort {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerDomainService customerDomainService;

    @Override
    public Mono<Customer> createCustomer(CreateCustomerDto createDto) {
        return Mono.fromCallable(() -> customerDomainService.createCustomer(createDto))
                .flatMap(customer -> checkDocumentNumberNotExists(customer.getDocumentNumber())
                        .flatMap(notExists -> customerRepositoryPort.storeCustomer(customer)));
    }

    @Override
    public Mono<Customer> findCustomerById(String customerId) {
        return customerRepositoryPort.getCustomerById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)));
    }

    @Override
    public Flux<Customer> findAllCustomers() {
        return customerRepositoryPort.getAllCustomers();
    }

    @Override
    public Mono<Customer> updateCustomer(String customerId, UpdateCustomerDto updateDto) {
        return findCustomerById(customerId)
                .flatMap(existingCustomer -> {
                    customerDomainService.updateCustomer(existingCustomer, updateDto);
                    return customerRepositoryPort.storeCustomer(existingCustomer);
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String customerId) {
        return findCustomerById(customerId)
                .flatMap(customer -> customerRepositoryPort.removeCustomerById(customerId));
    }

    private Mono<Boolean> checkDocumentNumberNotExists(String documentNumber) {
        return customerRepositoryPort.hasCustomerWithDocumentNumber(documentNumber)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidCustomerDataException(
                                "Ya existe un cliente con el número de documento: " + documentNumber));
                    }
                    return Mono.just(true);
                });
    }
}
