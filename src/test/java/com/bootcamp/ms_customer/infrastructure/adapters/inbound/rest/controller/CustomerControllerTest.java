package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.customer.dto.CustomerType;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.CustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
@DisplayName("Customer Controller Tests")
class CustomerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ManageCustomerUseCase customerService;

    @MockBean
    private CreateCustomerMapper createCustomerMapper;

    @MockBean
    private UpdateCustomerMapper updateCustomerMapper;

    @MockBean
    private CustomerResponseMapper customerResponseMapper;

    @Test
    @DisplayName("Should create customer successfully")
    void testCreateCustomerSuccess() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Juan");
        request.setLastName("Perez");
        request.setDocumentNumber("12345678");
        request.setEmail("juan@example.com");
        request.setPhoneNumber("+34912345678");

        CreateCustomerDto createDto = new CreateCustomerDto();
        createDto.setFirstName("Juan");
        createDto.setLastName("Perez");

        CustomerDto customerDto = new CustomerDto();
        customerDto.setCustomerId("cust-123");
        customerDto.setFirstName("Juan");
        customerDto.setLastName("Perez");
        customerDto.setStatus(CustomerStatus.ACTIVE);
        customerDto.setCreatedAt(LocalDateTime.now());

        CustomerResponse response = new CustomerResponse();
        response.setId("cust-123");
        response.setFirstName("Juan");
        response.setLastName("Perez");

        when(createCustomerMapper.toDomainDto(any())).thenReturn(createDto);
        when(customerService.createCustomer(any())).thenReturn(Mono.just(customerDto));
        when(customerResponseMapper.toResponse(any())).thenReturn(response);

        webTestClient
                .post()
                .uri("/api/v1/customers")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult();
    }

    @Test
    @DisplayName("Should return 400 when customer not found")
    void testGetCustomerNotFound() {
        String customerId = "non-existent-id";

        when(customerService.findCustomerById(customerId))
                .thenReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        webTestClient
                .get()
                .uri("/api/v1/customers/{id}", customerId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void testDeleteCustomerSuccess() {
        String customerId = "cust-123";

        when(customerService.deleteCustomer(customerId))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/api/v1/customers/{id}", customerId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
