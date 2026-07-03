package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.infrastructure.AbstractMongoTest;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper.CustomerEntityMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository.SpringDataCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Customer Repository Adapter MongoDB Integration Tests")
class CustomerRepositoryAdapterMongoTest extends AbstractMongoTest {

    @Autowired
    private SpringDataCustomerRepository repository;

    @Autowired
    private CustomerEntityMapper mapper;

    private CustomerRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new CustomerRepositoryAdapter(repository, mapper);
    }

    @Test
    @DisplayName("Should save and retrieve customer from MongoDB")
    void testSaveAndRetrieveCustomer() {
        Customer customer = createTestCustomer();

        repositoryAdapter.save(customer)
                .then(repositoryAdapter.findById(customer.getCustomerId()))
                .as(StepVerifier::create)
                .assertNext(retrieved -> {
                    assertThat(retrieved).isNotNull();
                    assertThat(retrieved.getCustomerId()).isEqualTo(customer.getCustomerId());
                    assertThat(retrieved.getFirstName()).isEqualTo("John");
                    assertThat(retrieved.getDocumentNumber()).isEqualTo("123456789");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find customer by document number")
    void testFindByDocumentNumber() {
        Customer customer = createTestCustomer();
        String documentNumber = "987654321";
        customer.setDocumentNumber(documentNumber);

        repositoryAdapter.save(customer)
                .then(repositoryAdapter.findByDocumentNumber(documentNumber))
                .as(StepVerifier::create)
                .assertNext(retrieved -> {
                    assertThat(retrieved.getDocumentNumber()).isEqualTo(documentNumber);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update existing customer")
    void testUpdateCustomer() {
        Customer customer = createTestCustomer();

        repositoryAdapter.save(customer)
                .doOnNext(saved -> {
                    saved.setFirstName("UpdatedName");
                    saved.setLastName("UpdatedLastName");
                })
                .flatMap(repositoryAdapter::save)
                .then(repositoryAdapter.findById(customer.getCustomerId()))
                .as(StepVerifier::create)
                .assertNext(updated -> {
                    assertThat(updated.getFirstName()).isEqualTo("UpdatedName");
                    assertThat(updated.getLastName()).isEqualTo("UpdatedLastName");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete customer")
    void testDeleteCustomer() {
        Customer customer = createTestCustomer();

        repositoryAdapter.save(customer)
                .then(repositoryAdapter.deleteById(customer.getCustomerId()))
                .then(repositoryAdapter.findById(customer.getCustomerId()))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should find customers by status")
    void testFindByStatus() {
        Customer customer = createTestCustomer();
        customer.setStatus(CustomerStatus.ACTIVE);

        repositoryAdapter.save(customer)
                .then(repositoryAdapter.findByStatus(CustomerStatus.ACTIVE).collectList())
                .as(StepVerifier::create)
                .assertNext(customers -> {
                    assertThat(customers).isNotEmpty();
                    assertThat(customers.stream()
                            .anyMatch(c -> c.getDocumentNumber().equals("123456789")))
                            .isTrue();
                })
                .verifyComplete();
    }

    private Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID().toString());
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setDocumentType(DocumentType.DNI);
        customer.setDocumentNumber("123456789");
        customer.setEmail("john@example.com");
        customer.setPhoneNumber("+34912345678");
        customer.setCustomerType(CustomerType.PERSONAL);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }
}
