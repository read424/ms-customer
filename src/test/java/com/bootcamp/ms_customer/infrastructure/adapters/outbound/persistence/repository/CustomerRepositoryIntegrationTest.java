package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository;

import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Customer Repository Integration Tests")
class CustomerRepositoryIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should save customer successfully")
    void testSaveCustomer() {
        CustomerEntity customer = CustomerEntity.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("Juan")
                .lastName("Perez")
                .documentType(DocumentType.CEDULA_IDENTIDAD)
                .documentNumber("12345678")
                .email("juan@example.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.INDIVIDUAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customerRepository.save(customer)
                .as(StepVerifier::create)
                .assertNext(savedCustomer -> {
                    assertThat(savedCustomer).isNotNull();
                    assertThat(savedCustomer.getCustomerId()).isNotNull();
                    assertThat(savedCustomer.getFirstName()).isEqualTo("Juan");
                    assertThat(savedCustomer.getEmail()).isEqualTo("juan@example.com");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find customer by ID")
    void testFindByCustomerId() {
        String customerId = UUID.randomUUID().toString();
        CustomerEntity customer = CustomerEntity.builder()
                .customerId(customerId)
                .firstName("Maria")
                .lastName("Garcia")
                .documentType(DocumentType.CEDULA_IDENTIDAD)
                .documentNumber("87654321")
                .email("maria@example.com")
                .phoneNumber("+34934567890")
                .customerType(CustomerType.INDIVIDUAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        customerRepository.save(customer)
                .then(customerRepository.findById(customerId))
                .as(StepVerifier::create)
                .assertNext(foundCustomer -> {
                    assertThat(foundCustomer).isNotNull();
                    assertThat(foundCustomer.getFirstName()).isEqualTo("Maria");
                    assertThat(foundCustomer.getDocumentNumber()).isEqualTo("87654321");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find customer by document number")
    void testFindByDocumentNumber() {
        String documentNumber = "55667788";
        CustomerEntity customer = CustomerEntity.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("Carlos")
                .lastName("Lopez")
                .documentType(DocumentType.CEDULA_IDENTIDAD)
                .documentNumber(documentNumber)
                .email("carlos@example.com")
                .phoneNumber("+34945556677")
                .customerType(CustomerType.INDIVIDUAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        customerRepository.save(customer)
                .then(customerRepository.findByDocumentNumber(documentNumber))
                .as(StepVerifier::create)
                .assertNext(foundCustomer -> {
                    assertThat(foundCustomer).isNotNull();
                    assertThat(foundCustomer.getDocumentNumber()).isEqualTo(documentNumber);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete customer by ID")
    void testDeleteCustomer() {
        String customerId = UUID.randomUUID().toString();
        CustomerEntity customer = CustomerEntity.builder()
                .customerId(customerId)
                .firstName("Ana")
                .lastName("Martinez")
                .documentType(DocumentType.CEDULA_IDENTIDAD)
                .documentNumber("11223344")
                .email("ana@example.com")
                .phoneNumber("+34912223344")
                .customerType(CustomerType.INDIVIDUAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        customerRepository.save(customer)
                .then(customerRepository.deleteById(customerId))
                .then(customerRepository.findById(customerId))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should find customers by status")
    void testFindByStatus() {
        CustomerEntity activeCustomer = CustomerEntity.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("Test")
                .lastName("User")
                .documentType(DocumentType.CEDULA_IDENTIDAD)
                .documentNumber("99887766")
                .email("test@example.com")
                .customerType(CustomerType.INDIVIDUAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        customerRepository.save(activeCustomer)
                .then(customerRepository.findByStatus(CustomerStatus.ACTIVE).collectList())
                .as(StepVerifier::create)
                .assertNext(customers -> {
                    assertThat(customers).isNotEmpty();
                    assertThat(customers.stream()
                            .anyMatch(c -> c.getDocumentNumber().equals("99887766")))
                            .isTrue();
                })
                .verifyComplete();
    }
}
