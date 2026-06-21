package com.bootcamp.ms_customer.application.service;

import com.bootcamp.ms_customer.application.mapper.CustomerDomainMapper;
import com.bootcamp.ms_customer.application.ports.output.CachePort;
import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CustomerService")
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepositoryPort;

    @Mock
    private CustomerDomainMapper customerDomainMapper;

    @Mock
    private CachePort cachePort;

    @Mock
    private SearchKeyGenerator searchKeyGenerator;

    @InjectMocks
    private CustomerService customerService;

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
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("12345678")).thenReturn(Mono.just(false));
        when(customerRepositoryPort.storeCustomer(any(Customer.class))).thenReturn(Mono.just(customer));
        when(cachePort.invalidateAllCustomerListCaches()).thenReturn(Mono.empty());

        StepVerifier.create(customerService.createCustomer(createDto))
                .expectNextMatches(saved -> saved.getDocumentNumber().equals("12345678") && saved.getFirstName().equals("Juan"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).hasCustomerWithDocumentNumber("12345678");
        verify(customerRepositoryPort, times(1)).storeCustomer(any(Customer.class));
        verify(cachePort, times(1)).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("Debe fallar al crear cliente PERSONAL sin firstName")
    void testCreatePersonalCustomerWithoutFirstName() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .lastName("Pérez")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .lastName("Pérez")
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);

        StepVerifier.create(customerService.createCustomer(createDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).hasCustomerWithDocumentNumber(anyString());
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al crear cliente PERSONAL sin lastName")
    void testCreatePersonalCustomerWithoutLastName() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);

        StepVerifier.create(customerService.createCustomer(createDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).hasCustomerWithDocumentNumber(anyString());
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al crear cliente BUSINESS sin businessName")
    void testCreateBusinessCustomerWithoutBusinessName() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);

        StepVerifier.create(customerService.createCustomer(createDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).hasCustomerWithDocumentNumber(anyString());
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al crear cliente con documento duplicado")
    void testCreateCustomerWithDuplicateDocument() {
        CreateCustomerDto createDto = CreateCustomerDto.builder()
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(customerDomainMapper.toCustomer(createDto)).thenReturn(customer);
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("12345678")).thenReturn(Mono.just(true));

        StepVerifier.create(customerService.createCustomer(createDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).hasCustomerWithDocumentNumber("12345678");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
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

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.findCustomerById("customer-123"))
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
    }

    @Test
    @DisplayName("Debe fallar al obtener cliente no encontrado")
    void testFindCustomerByIdNotFound() {
        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerService.findCustomerById("customer-123"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
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

        when(customerRepositoryPort.getAllCustomers()).thenReturn(Flux.just(customer1, customer2));

        StepVerifier.create(customerService.findAllCustomers())
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getAllCustomers();
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

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(existingCustomer));
        when(customerRepositoryPort.storeCustomer(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
        when(cachePort.invalidateCustomerDetailCache("customer-123")).thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches()).thenReturn(Mono.empty());

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectNextMatches(updated -> updated.getFirstName().equals("Juan Pablo") && updated.getEmail().equals("juanpablo@example.com"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(1)).storeCustomer(any(Customer.class));
        verify(cachePort, times(1)).invalidateCustomerDetailCache("customer-123");
        verify(cachePort, times(1)).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("Debe fallar al actualizar cliente no encontrado")
    void testUpdateCustomerNotFound() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder().firstName("Juan").build();

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
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

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(customer));
        when(customerRepositoryPort.removeCustomerById("customer-123")).thenReturn(Mono.empty());
        when(cachePort.invalidateCustomerDetailCache("customer-123")).thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches()).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer("customer-123"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(1)).removeCustomerById("customer-123");
        verify(cachePort, times(1)).invalidateCustomerDetailCache("customer-123");
        verify(cachePort, times(1)).invalidateAllCustomerListCaches();
    }

    @Test
    @DisplayName("Debe fallar al eliminar cliente no encontrado")
    void testDeleteCustomerNotFound() {
        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer("customer-123"))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepositoryPort, times(0)).removeCustomerById(anyString());
    }

    @Test
    @DisplayName("Debe fallar al actualizar cliente PERSONAL con estado INACTIVE")
    void testUpdatePersonalCustomerInactiveStatus() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .firstName("Juan Pablo")
                .build();

        Customer inactiveCustomer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .status(CustomerStatus.INACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(inactiveCustomer));

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al actualizar cliente BUSINESS con estado INACTIVE")
    void testUpdateBusinessCustomerInactiveStatus() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .email("newemail@example.com")
                .build();

        Customer inactiveCustomer = Customer.builder()
                .customerId("customer-456")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .email("business@example.com")
                .status(CustomerStatus.INACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-456")).thenReturn(Mono.just(inactiveCustomer));

        StepVerifier.create(customerService.updateCustomer("customer-456", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-456");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar actualizar businessName en cliente PERSONAL")
    void testUpdatePersonalCustomerBusinessName() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .businessName("Invalid Name")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar actualizar documentNumber en cliente BUSINESS")
    void testUpdateBusinessCustomerDocumentNumber() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .documentNumber("20987654321")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-456")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-456")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.updateCustomer("customer-456", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-456");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar actualizar firstName en cliente BUSINESS")
    void testUpdateBusinessCustomerFirstName() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .firstName("Juan")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-456")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-456")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.updateCustomer("customer-456", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-456");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar actualizar lastName en cliente BUSINESS")
    void testUpdateBusinessCustomerLastName() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .lastName("Pérez")
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-456")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-456")).thenReturn(Mono.just(customer));

        StepVerifier.create(customerService.updateCustomer("customer-456", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-456");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe permitir actualizar email y phoneNumber en cliente PERSONAL")
    void testUpdatePersonalCustomerAllowedFields() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .email("newemail@example.com")
                .phoneNumber("999999999")
                .build();

        Customer existingCustomer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@example.com")
                .phoneNumber("987654321")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(existingCustomer));
        when(customerRepositoryPort.storeCustomer(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
        when(cachePort.invalidateCustomerDetailCache("customer-123")).thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches()).thenReturn(Mono.empty());

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectNextMatches(updated -> updated.getEmail().equals("newemail@example.com") && updated.getPhoneNumber().equals("999999999"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(1)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe permitir actualizar email y phoneNumber en cliente BUSINESS")
    void testUpdateBusinessCustomerAllowedFields() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .email("newemail@business.com")
                .phoneNumber("555555555")
                .build();

        Customer existingCustomer = Customer.builder()
                .customerId("customer-456")
                .customerType(CustomerType.BUSINESS)
                .documentType(DocumentType.RUC)
                .documentNumber("20123456789")
                .businessName("Empresa S.A.")
                .email("business@example.com")
                .phoneNumber("987654321")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-456")).thenReturn(Mono.just(existingCustomer));
        when(customerRepositoryPort.storeCustomer(any(Customer.class))).thenReturn(Mono.just(existingCustomer));
        when(cachePort.invalidateCustomerDetailCache("customer-456")).thenReturn(Mono.empty());
        when(cachePort.invalidateAllCustomerListCaches()).thenReturn(Mono.empty());

        StepVerifier.create(customerService.updateCustomer("customer-456", updateDto))
                .expectNextMatches(updated -> updated.getEmail().equals("newemail@business.com") && updated.getPhoneNumber().equals("555555555"))
                .verifyComplete();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-456");
        verify(customerRepositoryPort, times(1)).storeCustomer(any(Customer.class));
    }

    @Test
    @DisplayName("Debe fallar al intentar cambiar documentNumber por uno duplicado")
    void testUpdateCustomerDuplicateDocumentNumber() {
        UpdateCustomerDto updateDto = UpdateCustomerDto.builder()
                .documentNumber("99999999")
                .build();

        Customer existingCustomer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .lastName("Pérez")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepositoryPort.getCustomerById("customer-123")).thenReturn(Mono.just(existingCustomer));
        when(customerRepositoryPort.hasCustomerWithDocumentNumber("99999999")).thenReturn(Mono.just(true));

        StepVerifier.create(customerService.updateCustomer("customer-123", updateDto))
                .expectError(InvalidCustomerDataException.class)
                .verify();

        verify(customerRepositoryPort, times(1)).getCustomerById("customer-123");
        verify(customerRepositoryPort, times(0)).storeCustomer(any(Customer.class));
    }
}
