package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.customer.dto.CustomerType;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(CustomerController.class)
@DisplayName("Customer Controller WebFlux Tests")
class CustomerControllerIntegrationTest {

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
    @DisplayName("Should create personal customer and return 201 Created")
    void testCreatePersonalCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("Juan");
        request.setLastName("Perez");
        request.setDocumentNumber("12345678");
        request.setEmail("juan.perez@example.com");
        request.setPhoneNumber("+34912345678");
        request.setCustomerType(CustomerType.PERSONAL);

        CreateCustomerDto domainDto = new CreateCustomerDto();
        domainDto.setFirstName("Juan");

        Customer createdCustomer = new Customer();
        createdCustomer.setCustomerId(UUID.randomUUID().toString());
        createdCustomer.setFirstName("Juan");
        createdCustomer.setLastName("Perez");
        createdCustomer.setStatus(CustomerStatus.ACTIVE);
        createdCustomer.setCreatedAt(LocalDateTime.now());

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(createdCustomer.getCustomerId());
        response.setFirstName("Juan");
        response.setLastName("Perez");

        when(createCustomerMapper.toDomainDto(any())).thenReturn(domainDto);
        when(customerService.createCustomer(any())).thenReturn(Mono.just(createdCustomer));
        when(customerResponseMapper.toResponse(any())).thenReturn(response);

        webTestClient
                .post()
                .uri("/api/v1/customers")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Juan")
                .jsonPath("$.lastName").isEqualTo("Perez");
    }

    @Test
    @DisplayName("Should return customer by ID")
    void testFindCustomerById() {
        String customerId = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFirstName("Maria");
        customer.setLastName("Garcia");
        customer.setStatus(CustomerStatus.ACTIVE);

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(customerId);
        response.setFirstName("Maria");
        response.setLastName("Garcia");

        when(customerService.findCustomerById(customerId)).thenReturn(Mono.just(customer));
        when(customerResponseMapper.toResponse(any())).thenReturn(response);

        webTestClient
                .get()
                .uri("/api/v1/customers/{id}", customerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Maria");
    }

    @Test
    @DisplayName("Should delete customer and return 204 No Content")
    void testDeleteCustomer() {
        String customerId = UUID.randomUUID().toString();

        when(customerService.deleteCustomer(customerId)).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/api/v1/customers/{id}", customerId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
