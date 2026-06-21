package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.api.CustomerApi;
import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.CustomerPageResponse;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.customer.dto.CustomerType;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final ManageCustomerUseCase customerService;
    private final CreateCustomerMapper createCustomerMapper;
    private final CustomerResponseMapper customerResponseMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            Mono<CreateCustomerRequest> createCustomerRequest,
            ServerWebExchange exchange) {
        return createCustomerRequest
                .flatMap(request -> customerService
                        .createCustomer(createCustomerMapper.toDomainDto(request))
                        .map(customerResponseMapper::toResponse)
                        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response)));
    }

    @Override
    public Mono<ResponseEntity<CustomerPageResponse>> findAllCustomers(
            Integer page,
            Integer size,
            CustomerType type,
            ServerWebExchange exchange) {

        int pageNum = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        com.bootcamp.ms_customer.domain.model.enums.CustomerType domainType = (type != null)
                ? com.bootcamp.ms_customer.domain.model.enums.CustomerType.valueOf(type.name())
                : null;

        return customerService.findCustomers(pageNum, pageSize, domainType)
                .map(paginated -> {
                    CustomerPageResponse response = new CustomerPageResponse();
                    response.setContent(paginated.getContent().stream()
                            .map(customerResponseMapper::toResponse)
                            .toList());
                    response.setPageNumber(paginated.getPageNumber());
                    response.setPageSize(paginated.getPageSize());
                    response.setTotalElements((int) paginated.getTotalElements());
                    response.setTotalPages(paginated.getTotalPages());
                    response.setIsLast(paginated.isLast());
                    return ResponseEntity.ok(response);
                });
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> findCustomerById(
            String customerId,
            ServerWebExchange exchange) {
        return customerService.findCustomerById(customerId)
                .map(customerResponseMapper::toResponse)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    if (error instanceof CustomerNotFoundException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.error(error);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> updateCustomer(
            String customerId,
            ServerWebExchange exchange) {
        return Mono.empty().then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(
            String customerId,
            ServerWebExchange exchange) {
        return customerService.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
