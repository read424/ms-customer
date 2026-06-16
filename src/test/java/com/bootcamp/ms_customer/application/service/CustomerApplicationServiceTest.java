package com.bootcamp.ms_customer.application.service;

import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.domain.service.CustomerDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CustomerApplicationService")
class CustomerApplicationServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    @Mock
    private CustomerDomainService customerDomainService;

    @InjectMocks
    private CustomerApplicationService customerApplicationService;

    @Test
    @DisplayName("Debe crear cliente exitosamente")
    void testCreateCustomerSuccess() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phoneNumber("987654321")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phoneNumber("987654321")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerDomainService.createCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.existsByDocumentNumber("12345678")).thenReturn(Mono.just(false));
        when(customerRepositoryPort.save(any(Customer.class))).thenReturn(Mono.just(customer));

        StepVerifier.create(customerApplicationService.createCustomer(createDto))
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).existsByDocumentNumber("12345678");
        verify(customerRepositoryPort, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al crear cliente con documento duplicado")
    void testCreateCustomerWithDuplicateDocument() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .build();

        Customer customer = Customer.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerDomainService.createCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.existsByDocumentNumber("12345678")).thenReturn(Mono.just(true));

        StepVerifier.create(customerApplicationService.createCustomer(createDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).existsByDocumentNumber("12345678");
        verify(customerRepositoryPort, times(0)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Debe obtener cliente por ID")
    void testFindCustomerByIdSuccess() {
        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerApplicationService.findCustomerById("customer-123"))
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).findById("customer-123");
    }

    @Test
    @DisplayName("Debe fallar al obtener cliente no encontrado")
    void testFindCustomerByIdNotFound() {
        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerApplicationService.findCustomerById("customer-123"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).findById("customer-123");
    }

    @Test
    @DisplayName("Debe obtener todos los clientes")
    void testFindAllCustomers() {
        Customer customer1 = Customer.builder()
                .customerId("customer-1")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("11111111")
                .firstName("Juan")
                .build();

        Customer customer2 = Customer.builder()
                .customerId("customer-2")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .build();

        when(customerRepositoryPort.findAll()).thenReturn(Flux.just(customer1, customer2));

        StepVerifier.create(customerApplicationService.findAllCustomers())
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe actualizar cliente exitosamente")
    void testUpdateCustomerSuccess() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .firstName("Juan Pablo")
                .email("juanpablo@example.com")
                .build();

        Customer existingCustomer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer updatedCustomer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan Pablo")
                .lastName("Pérez")
                .email("juanpablo@example.com")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.just(existingCustomer));
        when(customerRepositoryPort.save(any(Customer.class))).thenReturn(Mono.just(updatedCustomer));

        StepVerifier.create(customerApplicationService.updateCustomer("customer-123", updateDto))
                .expectNext(updatedCustomer)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).findById("customer-123");
        verify(customerRepositoryPort, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al actualizar cliente no encontrado")
    void testUpdateCustomerNotFound() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder().firstName("Juan").build();

        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerApplicationService.updateCustomer("customer-123", updateDto))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Debe eliminar cliente exitosamente")
    void testDeleteCustomerSuccess() {
        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .build();

        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.just(customer));
        when(customerRepositoryPort.deleteById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerApplicationService.deleteCustomer("customer-123"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).findById("customer-123");
        verify(customerRepositoryPort, times(1)).deleteById("customer-123");
    }

    @Test
    @DisplayName("Debe fallar al eliminar cliente no encontrado")
    void testDeleteCustomerNotFound() {
        when(customerRepositoryPort.findById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerApplicationService.deleteCustomer("customer-123"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).deleteById(anyString());
    }
}
