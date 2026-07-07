package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bootcamp.customer.dto.CreateCustomerRequest;
import com.bootcamp.customer.dto.CustomerResponse;
import com.bootcamp.customer.dto.CustomerTypeRequest;
import com.bootcamp.customer.dto.DocumentType;
import com.bootcamp.customer.dto.UpdateCustomerRequest;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapperImpl;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapperImpl;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapperImpl;
import com.bootcamp.ms_customer.infrastructure.config.GlobalExceptionHandler;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Controller Unit Tests")
class CustomerControllerIntegrationTest {

    @Mock
    private ManageCustomerUseCase manageCustomerUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        var controller = new CustomerController(
                manageCustomerUseCase,
                new CreateCustomerMapperImpl(),
                new UpdateCustomerMapperImpl(),
                new CustomerResponseMapperImpl()
        );

        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @Test
    @DisplayName("should create customer successfully")
    void shouldCreateCustomerSuccessfully() {
        var customer = createTestCustomer();
        when(manageCustomerUseCase.createCustomer(any())).thenReturn(Mono.just(customer));

        var request = new CreateCustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDocumentNumber("12345678A");
        request.setDocumentType(DocumentType.DNI);
        request.setEmail("john@example.com");
        request.setPhoneNumber("+34912345678");
        request.setCustomerType(CustomerTypeRequest.PERSONAL);

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.documentNumber").isEqualTo("12345678A");
    }

    @Test
    @DisplayName("should find customer by id")
    void shouldFindCustomerById() {
        var customer = createTestCustomer();
        when(manageCustomerUseCase.findCustomerById("CUST-001")).thenReturn(Mono.just(customer));

        webTestClient.get()
                .uri("/api/v1/customers/CUST-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.customerId").isEqualTo("CUST-001");
    }

    @Test
    @DisplayName("should return 400 when customer not found")
    void shouldReturn400WhenCustomerNotFound() {
        when(manageCustomerUseCase.findCustomerById("non-existent"))
                .thenReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        webTestClient.get()
                .uri("/api/v1/customers/non-existent")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("should find all customers with pagination")
    void shouldFindAllCustomersWithPagination() {
        var customer1 = createTestCustomer();
        var customer2 = createTestCustomer();
        customer2.setCustomerId("CUST-002");

        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer1, customer2))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(2)
                .totalPages(1)
                .isLast(true)
                .build();

        when(manageCustomerUseCase.findCustomers(1, 10, null))
                .thenReturn(Mono.just(paginated));

        webTestClient.get()
                .uri("/api/v1/customers?page=1&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.pageNumber").isEqualTo(1)
                .jsonPath("$.totalElements").isEqualTo(2);
    }

    @Test
    @DisplayName("should find customers with type filter")
    void shouldFindCustomersWithTypeFilter() {
        var customer = createTestCustomer();

        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(manageCustomerUseCase.findCustomers(
                eq(1), eq(10),
                eq(com.bootcamp.ms_customer.domain.model.enums.CustomerType.PERSONAL)))
                .thenReturn(Mono.just(paginated));

        webTestClient.get()
                .uri("/api/v1/customers?page=1&size=10&type=PERSONAL")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1);
    }

    @Test
    @DisplayName("should use default pagination when not specified")
    void shouldUseDefaultPaginationWhenNotSpecified() {
        var customer = createTestCustomer();

        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(manageCustomerUseCase.findCustomers(1, 10, null))
                .thenReturn(Mono.just(paginated));

        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pageNumber").isEqualTo(1)
                .jsonPath("$.pageSize").isEqualTo(10);
    }

    @Test
    @DisplayName("should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        var customer = createTestCustomer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");

        when(manageCustomerUseCase.updateCustomer(eq("CUST-001"), any()))
                .thenReturn(Mono.just(customer));

        var updateRequest = new UpdateCustomerRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("jane@example.com");
        updateRequest.setPhoneNumber("+34987654321");

        webTestClient.put()
                .uri("/api/v1/customers/CUST-001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.firstName").isEqualTo("Jane")
                .jsonPath("$.lastName").isEqualTo("Smith");
    }

    @Test
    @DisplayName("should return 404 when updating non-existent customer")
    void shouldReturn404WhenUpdatingNonExistentCustomer() {
        when(manageCustomerUseCase.updateCustomer(eq("non-existent"), any()))
                .thenReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        var updateRequest = new UpdateCustomerRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setEmail("jane@example.com");

        webTestClient.put()
                .uri("/api/v1/customers/non-existent")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() {
        when(manageCustomerUseCase.deleteCustomer("CUST-001"))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/customers/CUST-001")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("should handle multiple create operations")
    void shouldHandleMultipleCreateOperations() {
        var customer = createTestCustomer();
        when(manageCustomerUseCase.createCustomer(any())).thenReturn(Mono.just(customer));

        var request = new CreateCustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDocumentNumber("12345678A");
        request.setDocumentType(DocumentType.DNI);
        request.setEmail("john@example.com");
        request.setPhoneNumber("+34912345678");
        request.setCustomerType(CustomerTypeRequest.PERSONAL);

        for (int i = 0; i < 3; i++) {
            webTestClient.post()
                    .uri("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }
    }

    @Test
    @DisplayName("should return empty list when no customers exist")
    void shouldReturnEmptyListWhenNoCustomersExist() {
        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList())
                .pageNumber(1)
                .pageSize(10)
                .totalElements(0)
                .totalPages(0)
                .isLast(true)
                .build();

        when(manageCustomerUseCase.findCustomers(1, 10, null))
                .thenReturn(Mono.just(paginated));

        webTestClient.get()
                .uri("/api/v1/customers?page=1&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0)
                .jsonPath("$.totalElements").isEqualTo(0);
    }

    @Test
    @DisplayName("should handle pagination with multiple pages")
    void shouldHandlePaginationWithMultiplePages() {
        var customers = Arrays.asList(
                createTestCustomer(),
                createTestCustomer()
        );

        var paginated = PaginatedResult.<Customer>builder()
                .content(customers)
                .pageNumber(1)
                .pageSize(2)
                .totalElements(5)
                .totalPages(3)
                .isLast(false)
                .build();

        when(manageCustomerUseCase.findCustomers(1, 2, null))
                .thenReturn(Mono.just(paginated));

        webTestClient.get()
                .uri("/api/v1/customers?page=1&size=2")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.pageNumber").isEqualTo(1)
                .jsonPath("$.totalPages").isEqualTo(3)
                .jsonPath("$.isLast").isEqualTo(false);
    }

    private Customer createTestCustomer() {
        return Customer.builder()
                .customerId("CUST-001")
                .firstName("John")
                .lastName("Doe")
                .documentType(com.bootcamp.ms_customer.domain.model.enums.DocumentType.DNI)
                .documentNumber("12345678A")
                .email("john@example.com")
                .phoneNumber("+34912345678")
                .customerType(com.bootcamp.ms_customer.domain.model.enums.CustomerType.PERSONAL)
                .status(CustomerStatus.ACTIVE)
                .build();
    }
}
