package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.api.CustomerApi;
import com.bootcamp.customer.dto.UpdateCustomerRequest;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CustomerController - Update")
class CustomerControllerTest {

    @Mock
    private ManageCustomerUseCase customerService;

    @Mock
    private UpdateCustomerMapper updateCustomerMapper;

    @Mock
    private CustomerResponseMapper customerResponseMapper;

    @Mock
    private ServerWebExchange exchange;

    @InjectMocks
    private CustomerController customerController;

    @Test
    @DisplayName("Debe retornar 404 cuando cliente no existe")
    void testUpdateCustomerNotFound() {
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setEmail("test@example.com");

        when(updateCustomerMapper.toDomainDto(any())).thenReturn(null);
        when(customerService.updateCustomer(anyString(), any())).thenReturn(
                Mono.error(new CustomerNotFoundException("customer-123"))
        );

        var result = customerController.updateCustomer("customer-123", Mono.just(request), exchange);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debe retornar 400 con mensaje cuando hay error de validación")
    void testUpdateCustomerInvalidData() {
        UpdateCustomerRequest request = new UpdateCustomerRequest();
        request.setBusinessName("Invalid");

        when(updateCustomerMapper.toDomainDto(any())).thenReturn(null);
        when(customerService.updateCustomer(anyString(), any())).thenReturn(
                Mono.error(new InvalidCustomerDataException("No se puede actualizar 'businessName' en un cliente de tipo PERSONAL"))
        );

        var result = customerController.updateCustomer("customer-123", Mono.just(request), exchange);

        StepVerifier.create(result)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }
}
