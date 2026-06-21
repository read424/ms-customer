package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.CustomerRepositoryAdapter;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper.CustomerPersistenceMapper;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CustomerRepositoryAdapter")
class CustomerRepositoryAdapterTest {

    @Mock
    private SpringDataCustomerRepository springDataRepository;

    @Mock
    private CustomerPersistenceMapper customerPersistenceMapper;

    @InjectMocks
    private CustomerRepositoryAdapter customerRepositoryAdapter;

    @Test
    @DisplayName("Debe guardar cliente exitosamente")
    void testSaveSuccess() {
        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomerEntity entity = CustomerEntity.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(customerPersistenceMapper.toEntity(customer)).thenReturn(entity);
        when(springDataRepository.save(entity)).thenReturn(Mono.just(entity));
        when(customerPersistenceMapper.toDomain(entity)).thenReturn(customer);

        StepVerifier.create(customerRepositoryAdapter.storeCustomer(customer))
                .expectNext(customer)
                .verifyComplete();

        verify(springDataRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Debe obtener cliente por ID")
    void testFindByIdSuccess() {
        CustomerEntity entity = CustomerEntity.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer customer = Customer.builder()
                .customerId("customer-123")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("12345678")
                .firstName("Juan")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(springDataRepository.findById("customer-123")).thenReturn(Mono.just(entity));
        when(customerPersistenceMapper.toDomain(entity)).thenReturn(customer);

        StepVerifier.create(customerRepositoryAdapter.getCustomerById("customer-123"))
                .expectNext(customer)
                .verifyComplete();

        verify(springDataRepository, times(1)).findById("customer-123");
    }

    @Test
    @DisplayName("Debe retornar vacío cuando cliente no existe")
    void testFindByIdNotFound() {
        when(springDataRepository.findById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerRepositoryAdapter.getCustomerById("customer-123"))
                .verifyComplete();

        verify(springDataRepository, times(1)).findById("customer-123");
    }

    @Test
    @DisplayName("Debe obtener todos los clientes")
    void testFindAllSuccess() {
        CustomerEntity entity1 = CustomerEntity.builder()
                .customerId("customer-1")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("11111111")
                .firstName("Juan")
                .build();

        Customer customer1 = Customer.builder()
                .customerId("customer-1")
                .customerType(CustomerType.PERSONAL)
                .documentType(DocumentType.DNI)
                .documentNumber("11111111")
                .firstName("Juan")
                .build();

        when(springDataRepository.findAll()).thenReturn(Flux.just(entity1));
        when(customerPersistenceMapper.toDomain(entity1)).thenReturn(customer1);

        StepVerifier.create(customerRepositoryAdapter.getAllCustomers())
                .expectNext(customer1)
                .verifyComplete();

        verify(springDataRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe eliminar cliente exitosamente")
    void testDeleteByIdSuccess() {
        when(springDataRepository.deleteById("customer-123")).thenReturn(Mono.empty());

        StepVerifier.create(customerRepositoryAdapter.removeCustomerById("customer-123"))
                .verifyComplete();

        verify(springDataRepository, times(1)).deleteById("customer-123");
    }

    @Test
    @DisplayName("Debe verificar existencia de documento")
    void testExistsByDocumentNumber() {
        when(springDataRepository.existsByDocumentNumber("12345678")).thenReturn(Mono.just(true));

        StepVerifier.create(customerRepositoryAdapter.hasCustomerWithDocumentNumber("12345678"))
                .expectNext(true)
                .verifyComplete();

        verify(springDataRepository, times(1)).existsByDocumentNumber("12345678");
    }
}
