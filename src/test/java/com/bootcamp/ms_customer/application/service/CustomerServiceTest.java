package com.bootcamp.ms_customer.application.service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bootcamp.ms_customer.application.ports.output.CachePort;
import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.mapper.CustomerDomainMapper;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import reactor.core.publisher.Flux;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    @Mock
    private CustomerDomainMapper customerDomainMapper;

    @Mock
    private CachePort cachePort;

    @Mock
    private SearchKeyGenerator searchKeyGenerator;

    private CustomerService customerService;

    @BeforeEach
    void setup() {
        customerService = new CustomerService(
                customerRepositoryPort,
                customerDomainMapper,
                cachePort,
                searchKeyGenerator
        );
    }

    // ──────────────────────────────────────────────────────────────────
    // createCustomer Tests
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should create personal customer successfully")
    void shouldCreatePersonalCustomerSuccessfully() {
        var createDto = new CreateCustomerDto();
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setDocumentNumber("12345678A");
        createDto.setDocumentType(DocumentType.DNI);
        createDto.setCustomerType(CustomerType.PERSONAL);
        createDto.setEmail("john@example.com");
        createDto.setPhoneNumber("+34912345678");

        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("12345678A"))
                .thenReturn(Mono.just(false));
        when(customerRepositoryPort.storeCustomer(customer))
                .thenReturn(Mono.just(customer));
        when(cachePort.invalidateAllCustomerListCaches())
                .thenReturn(Mono.empty());

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .assertNext(saved -> {
                    assertEquals("CUST-001", saved.getCustomerId());
                    assertEquals("John", saved.getFirstName());
                })
                .verifyComplete();

        verify(customerRepositoryPort).hasCustomerWithDocumentNumber("12345678A");
        verify(customerRepositoryPort).storeCustomer(customer);
        verify(cachePort).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("should create business customer successfully")
    void shouldCreateBusinessCustomerSuccessfully() {
        var createDto = new CreateCustomerDto();
        createDto.setBusinessName("Acme Corp");
        createDto.setDocumentNumber("ABC123");
        createDto.setDocumentType(DocumentType.RUC);
        createDto.setCustomerType(CustomerType.BUSINESS);
        createDto.setEmail("contact@acme.com");
        createDto.setPhoneNumber("+34912345678");

        var customer = Customer.builder()
                .customerId("CUST-002")
                .businessName("Acme Corp")
                .documentNumber("ABC123")
                .documentType(DocumentType.RUC)
                .customerType(CustomerType.BUSINESS)
                .email("contact@acme.com")
                .phoneNumber("+34912345678")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("ABC123"))
                .thenReturn(Mono.just(false));
        when(customerRepositoryPort.storeCustomer(customer))
                .thenReturn(Mono.just(customer));
        when(cachePort.invalidateAllCustomerListCaches())
                .thenReturn(Mono.empty());

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .assertNext(saved -> assertEquals("CUST-002", saved.getCustomerId()))
                .verifyComplete();

        verify(cachePort).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("should fail when creating personal customer without firstName")
    void shouldFailWhenCreatePersonalWithoutFirstName() {
        var createDto = new CreateCustomerDto();
        createDto.setLastName("Doe");
        createDto.setDocumentNumber("12345678A");
        createDto.setDocumentType(DocumentType.DNI);
        createDto.setCustomerType(CustomerType.PERSONAL);

        var customer = Customer.builder()
                .customerId("CUST-001")
                .lastName("Doe")
                .documentNumber("12345678A")
                .documentType(DocumentType.DNI)
                .customerType(CustomerType.PERSONAL)
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail when creating business customer without businessName")
    void shouldFailWhenCreateBusinessWithoutBusinessName() {
        var createDto = new CreateCustomerDto();
        createDto.setDocumentNumber("ABC123");
        createDto.setDocumentType(DocumentType.RUC);
        createDto.setCustomerType(CustomerType.BUSINESS);

        var customer = Customer.builder()
                .customerId("CUST-002")
                .documentNumber("ABC123")
                .documentType(DocumentType.RUC)
                .customerType(CustomerType.BUSINESS)
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail when document number already exists")
    void shouldFailWhenDocumentNumberExists() {
        var createDto = new CreateCustomerDto();
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setDocumentNumber("12345678A");
        createDto.setDocumentType(DocumentType.DNI);
        createDto.setCustomerType(CustomerType.PERSONAL);

        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("12345678A"))
                .thenReturn(Mono.just(true));

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    // ──────────────────────────────────────────────────────────────────
    // findCustomerById Tests
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should find customer by id successfully")
    void shouldFindCustomerByIdSuccessfully() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(customer));

        customerService.findCustomerById("CUST-001")
                .as(StepVerifier::create)
                .assertNext(found -> assertEquals("CUST-001", found.getCustomerId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail when customer not found by id")
    void shouldFailWhenCustomerNotFound() {
        when(customerRepositoryPort.getCustomerById("CUST-999"))
                .thenReturn(Mono.empty());

        customerService.findCustomerById("CUST-999")
                .as(StepVerifier::create)
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    // ──────────────────────────────────────────────────────────────────
    // findCustomers (Paginated) Tests
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return cached customers on cache hit")
    void shouldReturnCachedCustomersOnHit() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        when(searchKeyGenerator.generateSearchKey(1, 10, null))
                .thenReturn("page:1:size:10:type:null");
        when(cachePort.getCustomerListBySearchKey("page:1:size:10:type:null"))
                .thenReturn(Mono.just(Optional.of(paginated)));
        // Mock repositorio para evitar NullPointerException si switchIfEmpty se ejecuta
        when(customerRepositoryPort.getCustomersPaginated(1, 10, null))
                .thenReturn(Mono.just(paginated));

        customerService.findCustomers(1, 10, null)
                .as(StepVerifier::create)
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(1, result.getTotalElements()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should fetch from repository and cache on cache miss")
    void shouldFetchAndCacheOnMiss() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        when(searchKeyGenerator.generateSearchKey(1, 10, null))
                .thenReturn("page:1:size:10:type:null");
        when(cachePort.getCustomerListBySearchKey("page:1:size:10:type:null"))
                .thenReturn(Mono.just(Optional.empty()));
        when(customerRepositoryPort.getCustomersPaginated(1, 10, null))
                .thenReturn(Mono.just(paginated));
        when(cachePort.cacheCustomerListBySearchKey("page:1:size:10:type:null", paginated))
                .thenReturn(Mono.empty());

        customerService.findCustomers(1, 10, null)
                .as(StepVerifier::create)
                .assertNext(result -> assertEquals(1, result.getTotalElements()))
                .verifyComplete();

        verify(cachePort).cacheCustomerListBySearchKey("page:1:size:10:type:null", paginated);
    }

    @Test
    @DisplayName("should find customers with type filter")
    void shouldFindCustomersWithTypeFilter() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var paginated = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        when(searchKeyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL))
                .thenReturn("page:1:size:10:type:PERSONAL");
        when(cachePort.getCustomerListBySearchKey("page:1:size:10:type:PERSONAL"))
                .thenReturn(Mono.just(Optional.empty()));
        when(customerRepositoryPort.getCustomersPaginated(1, 10, CustomerType.PERSONAL))
                .thenReturn(Mono.just(paginated));
        when(cachePort.cacheCustomerListBySearchKey(anyString(), any()))
                .thenReturn(Mono.empty());

        customerService.findCustomers(1, 10, CustomerType.PERSONAL)
                .as(StepVerifier::create)
                .assertNext(result -> assertEquals(1, result.getTotalElements()))
                .verifyComplete();
    }

    // ──────────────────────────────────────────────────────────────────
    // updateCustomer Tests
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should update personal customer successfully")
    void shouldUpdatePersonalCustomerSuccessfully() {
        var existing = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var updateDto = new UpdateCustomerDto();
        updateDto.setFirstName("Jane");
        updateDto.setEmail("jane@example.com");

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(existing));
        when(customerRepositoryPort.storeCustomer(any()))
                .thenReturn(Mono.just(existing));
        when(cachePort.invalidateCustomerDetailCache("CUST-001"))
                .thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches())
                .thenReturn(Mono.empty());

        customerService.updateCustomer("CUST-001", updateDto)
                .as(StepVerifier::create)
                .assertNext(updated -> assertEquals("CUST-001", updated.getCustomerId()))
                .verifyComplete();

        verify(customerRepositoryPort).storeCustomer(any());
        verify(cachePort).invalidateCustomerDetailCache("CUST-001");
        verify(cachePort).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("should fail updating personal customer with businessName")
    void shouldFailUpdatePersonalWithBusinessName() {
        var existing = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var updateDto = new UpdateCustomerDto();
        updateDto.setBusinessName("Invalid");

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(existing));

        customerService.updateCustomer("CUST-001", updateDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail updating business customer with documentNumber")
    void shouldFailUpdateBusinessWithDocumentNumber() {
        var existing = Customer.builder()
                .customerId("CUST-002")
                .businessName("Acme")
                .documentNumber("ABC123")
                .customerType(CustomerType.BUSINESS)
                .status(CustomerStatus.ACTIVE)
                .build();

        var updateDto = new UpdateCustomerDto();
        updateDto.setDocumentNumber("XYZ789");

        when(customerRepositoryPort.getCustomerById("CUST-002"))
                .thenReturn(Mono.just(existing));

        customerService.updateCustomer("CUST-002", updateDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail updating inactive customer")
    void shouldFailUpdatingInactiveCustomer() {
        var existing = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        existing.setStatus(CustomerStatus.INACTIVE);

        var updateDto = new UpdateCustomerDto();
        updateDto.setFirstName("Jane");

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(existing));

        customerService.updateCustomer("CUST-001", updateDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail updating with duplicate document number")
    void shouldFailUpdateWithDuplicateDocumentNumber() {
        var existing = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var updateDto = new UpdateCustomerDto();
        updateDto.setDocumentNumber("99999999Z");

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(existing));
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("99999999Z"))
                .thenReturn(Mono.just(true));

        customerService.updateCustomer("CUST-001", updateDto)
                .as(StepVerifier::create)
                .expectError(InvalidCustomerDataException.class)
                .verify();
    }

    // ──────────────────────────────────────────────────────────────────
    // deleteCustomer Tests
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);

        when(customerRepositoryPort.getCustomerById("CUST-001"))
                .thenReturn(Mono.just(customer));
        when(customerRepositoryPort.removeCustomerById("CUST-001"))
                .thenReturn(Mono.empty());
        when(cachePort.invalidateCustomerDetailCache("CUST-001"))
                .thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches())
                .thenReturn(Mono.empty());

        customerService.deleteCustomer("CUST-001")
                .as(StepVerifier::create)
                .verifyComplete();

        verify(customerRepositoryPort).removeCustomerById("CUST-001");
        verify(cachePort).invalidateCustomerDetailCache("CUST-001");
        verify(cachePort).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("should fail deleting non-existent customer")
    void shouldFailDeletingNonExistentCustomer() {
        when(customerRepositoryPort.getCustomerById("CUST-999"))
                .thenReturn(Mono.empty());

        customerService.deleteCustomer("CUST-999")
                .as(StepVerifier::create)
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should get all customers successfully")
    void shouldGetAllCustomersSuccessfully() {
        var customer1 = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var customer2 = createCustomer("CUST-002", "Jane", "Smith", "87654321B", CustomerType.PERSONAL);

        when(customerRepositoryPort.getAllCustomers())
                .thenReturn(Flux.just(customer1, customer2));

        customerService.findAllCustomers()
                .as(StepVerifier::create)
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();

        verify(customerRepositoryPort).getAllCustomers();
    }

    @Test
    @DisplayName("should return empty flux when no customers exist")
    void shouldReturnEmptyFluxWhenNoCustomersExist() {
        when(customerRepositoryPort.getAllCustomers())
                .thenReturn(Flux.empty());

        customerService.findAllCustomers()
                .as(StepVerifier::create)
                .verifyComplete();

        verify(customerRepositoryPort).getAllCustomers();
    }

    @Test
    @DisplayName("should fail validating business customer update with documentNumber")
    void shouldFailValidatingBusinessCustomerUpdateWithDocumentNumber() {
        var updateDto = new UpdateCustomerDto();
        updateDto.setDocumentNumber("87654321A");

        assertThrows(InvalidCustomerDataException.class, () -> {
            ReflectionTestUtils.invokeMethod(customerService, "validateBusinessCustomerUpdate", updateDto);
        });
    }

    @Test
    @DisplayName("should fail validating business customer update with firstName")
    void shouldFailValidatingBusinessCustomerUpdateWithFirstName() {
        var updateDto = new UpdateCustomerDto();
        updateDto.setFirstName("NewName");

        assertThrows(InvalidCustomerDataException.class, () -> {
            ReflectionTestUtils.invokeMethod(customerService, "validateBusinessCustomerUpdate", updateDto);
        });
    }

    @Test
    @DisplayName("should fail validating business customer update with lastName")
    void shouldFailValidatingBusinessCustomerUpdateWithLastName() {
        var updateDto = new UpdateCustomerDto();
        updateDto.setLastName("NewLastName");

        assertThrows(InvalidCustomerDataException.class, () -> {
            ReflectionTestUtils.invokeMethod(customerService, "validateBusinessCustomerUpdate", updateDto);
        });
    }

    @Test
    @DisplayName("should fail validating business customer update with businessName")
    void shouldFailValidatingBusinessCustomerUpdateWithBusinessName() {
        var updateDto = new UpdateCustomerDto();
        updateDto.setBusinessName("NewBusinessName");

        assertThrows(InvalidCustomerDataException.class, () -> {
            ReflectionTestUtils.invokeMethod(customerService, "validateBusinessCustomerUpdate", updateDto);
        });
    }

    @Test
    @DisplayName("should pass validating business customer update with email only")
    void shouldPassValidatingBusinessCustomerUpdateWithEmailOnly() {
        var updateDto = new UpdateCustomerDto();
        updateDto.setEmail("newemail@example.com");

        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(customerService, "validateBusinessCustomerUpdate", updateDto);
        });
    }

    @Test
    @DisplayName("should apply update fields for personal customer")
    void shouldApplyUpdateFieldsForPersonalCustomer() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var updateDto = new UpdateCustomerDto();
        updateDto.setFirstName("Jane");
        updateDto.setLastName("Smith");
        updateDto.setEmail("jane@example.com");
        updateDto.setPhoneNumber("+34987654321");

        ReflectionTestUtils.invokeMethod(customerService, "applyUpdateFields", customer, updateDto);

        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("jane@example.com", customer.getEmail());
        assertEquals("+34987654321", customer.getPhoneNumber());
    }

    @Test
    @DisplayName("should apply partial update fields")
    void shouldApplyPartialUpdateFields() {
        var customer = createCustomer("CUST-001", "John", "Doe", "12345678A", CustomerType.PERSONAL);
        var originalLastName = customer.getLastName();
        var updateDto = new UpdateCustomerDto();
        updateDto.setEmail("new@example.com");

        ReflectionTestUtils.invokeMethod(customerService, "applyUpdateFields", customer, updateDto);

        assertEquals("John", customer.getFirstName());
        assertEquals(originalLastName, customer.getLastName());
        assertEquals("new@example.com", customer.getEmail());
    }

    // ──────────────────────────────────────────────────────────────────
    // Helper Methods
    // ──────────────────────────────────────────────────────────────────

    private Customer createCustomer(String id, String firstName, String lastName, String documentNumber, CustomerType type) {
        LocalDateTime fixedTime = LocalDateTime.of(2026, Month.JULY, 4, 12, 0, 0);
        return Customer.builder()
                .customerId(id)
                .firstName(firstName)
                .lastName(lastName)
                .documentNumber(documentNumber)
                .documentType(DocumentType.DNI)
                .customerType(type)
                .email("email@example.com")
                .phoneNumber("+34912345678")
                .status(CustomerStatus.ACTIVE)
                .createdAt(fixedTime)
                .updatedAt(fixedTime)
                .build();
    }
}
