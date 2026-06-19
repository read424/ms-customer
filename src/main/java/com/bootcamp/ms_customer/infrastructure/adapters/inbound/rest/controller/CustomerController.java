package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.api.CustomerApi;
import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.UpdateCustomerRequest;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.ms_customer.application.ports.input.CustomerServicePort;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CustomerServicePort customerService;
    private final CreateCustomerMapper createCustomerMapper;
    private final UpdateCustomerMapper updateCustomerMapper;
    private final CustomerResponseMapper customerResponseMapper;

    @Override
    public Mono<ResponseEntity<Void>> createCustomer(
            Mono<CreateCustomerRequest> createCustomerRequest,
            ServerWebExchange exchange) {
        return createCustomerRequest
                .flatMap(request -> customerService
                        .createCustomer(createCustomerMapper.toDomainDto(request))
                        .map(customerResponseMapper::toResponse)
                        .map(response -> ResponseEntity.status(HttpStatus.CREATED).build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> findAllCustomers(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<Void>> findCustomerById(
            String customerId,
            ServerWebExchange exchange) {
        return customerService.findCustomerById(customerId)
                .map(customerResponseMapper::toResponse)
                .map(response -> ResponseEntity.ok().build());
    }

    @Override
    public Mono<ResponseEntity<Void>> updateCustomer(
            String customerId,
            ServerWebExchange exchange) {
        return Mono.empty().then(Mono.just(ResponseEntity.ok().build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(
            String customerId,
            ServerWebExchange exchange) {
        return customerService.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
