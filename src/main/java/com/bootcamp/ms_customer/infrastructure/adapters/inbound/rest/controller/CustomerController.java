package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.api.CustomerApi;
import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.CustomerPageResponse;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.customer.dto.CustomerTypeRequest;
import com.bootcamp.customer.dto.UpdateCustomerRequest;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final ManageCustomerUseCase manageCustomerUseCase;
    private final CreateCustomerMapper createCustomerMapper;
    private final UpdateCustomerMapper updateCustomerMapper;
    private final CustomerResponseMapper customerResponseMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            Mono<CreateCustomerRequest> createCustomerRequest,
            ServerWebExchange exchange) {
        return createCustomerRequest
                .flatMap(request -> manageCustomerUseCase
                        .createCustomer(createCustomerMapper.toDomainDto(request))
                        .map(customerResponseMapper::toResponse)
                        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response)));
    }

    @Override
    public Mono<ResponseEntity<CustomerPageResponse>> findAllCustomers(
            Integer page,
            Integer size,
            CustomerTypeRequest type,
            ServerWebExchange exchange) {

        int pageNum = (page != null) ? page : 1;
        int pageSize = (size != null) ? size : 10;
        com.bootcamp.ms_customer.domain.model.enums.CustomerType domainType = (type != null)
                ? com.bootcamp.ms_customer.domain.model.enums.CustomerType.valueOf(type.name())
                : null;

        return manageCustomerUseCase.findCustomers(pageNum, pageSize, domainType)
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
        return manageCustomerUseCase.findCustomerById(customerId)
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
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
            String customerId,
            Mono<UpdateCustomerRequest> updateCustomerRequest,
            ServerWebExchange exchange) {
        return updateCustomerRequest
                .flatMap(request -> manageCustomerUseCase.updateCustomer(customerId, updateCustomerMapper.toDomainDto(request))
                        .map(customerResponseMapper::toResponse)
                        .map(ResponseEntity::ok))
                .onErrorResume(error -> {
                    if (error instanceof CustomerNotFoundException) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                    return Mono.error(error);
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(
            String customerId,
            ServerWebExchange exchange) {
        return manageCustomerUseCase.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
