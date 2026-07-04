package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.infrastructure.test.persistence.base.AbstractPersistenceAdapterIntegrationTest;
import com.bootcamp.ms_customer.infrastructure.test.persistence.fixtures.CustomerTestFixture;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Customer Repository Adapter Integration Tests")
class CustomerRepositoryAdapterIntegrationTest extends AbstractPersistenceAdapterIntegrationTest {

    @Test
    @DisplayName("should store and retrieve customer successfully")
    void shouldStoreAndRetrieveCustomer() {
        var adapter = getBean(CustomerRepositoryAdapter.class);
        var customer = CustomerTestFixture.createValidPersonalCustomer();

        adapter.storeCustomer(customer)
                .flatMap(stored -> adapter.getCustomerById(stored.getCustomerId()))
                .as(StepVerifier::create)
                .assertNext(retrieved -> {
                    assertThat(retrieved.getFirstName()).isEqualTo("John");
                    assertThat(retrieved.getDocumentNumber()).isEqualTo("12345678A");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return empty when customer not found")
    void shouldReturnEmptyWhenCustomerNotFound() {
        var adapter = getBean(CustomerRepositoryAdapter.class);

        adapter.getCustomerById("non-existent-id")
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("should check customer existence by document number")
    void shouldCheckCustomerExistenceByDocumentNumber() {
        var adapter = getBean(CustomerRepositoryAdapter.class);
        var customer = CustomerTestFixture.createValidPersonalCustomer();

        adapter.storeCustomer(customer)
                .then(adapter.hasCustomerWithDocumentNumber(customer.getDocumentNumber()))
                .as(StepVerifier::create)
                .assertNext(exists -> assertThat(exists).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("should update customer successfully")
    void shouldUpdateCustomerSuccessfully() {
        var adapter = getBean(CustomerRepositoryAdapter.class);
        var customer = CustomerTestFixture.createValidPersonalCustomer();

        adapter.storeCustomer(customer)
                .flatMap(stored -> {
                    stored.setFirstName("Jane");
                    return adapter.storeCustomer(stored);
                })
                .flatMap(updated -> adapter.getCustomerById(updated.getCustomerId()))
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result.getFirstName()).isEqualTo("Jane"))
                .verifyComplete();
    }

    @Test
    @DisplayName("should delete customer by id")
    void shouldDeleteCustomerById() {
        var adapter = getBean(CustomerRepositoryAdapter.class);
        var customer = CustomerTestFixture.createValidPersonalCustomer();

        adapter.storeCustomer(customer)
                .flatMap(stored -> adapter.removeCustomerById(stored.getCustomerId())
                        .then(adapter.getCustomerById(stored.getCustomerId())))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("should retrieve all customers")
    void shouldRetrieveAllCustomers() {
        var adapter = getBean(CustomerRepositoryAdapter.class);

        adapter.storeCustomer(CustomerTestFixture.createValidPersonalCustomer())
                .then(adapter.storeCustomer(CustomerTestFixture.createValidBusinessCustomer()))
                .then(adapter.getAllCustomers().collectList())
                .as(StepVerifier::create)
                .assertNext(customers -> assertThat(customers).hasSize(2))
                .verifyComplete();
    }

    @Test
    @DisplayName("should paginate customers without filter")
    void shouldPaginateCustomersWithoutFilter() {
        var adapter = getBean(CustomerRepositoryAdapter.class);

        for (int i = 0; i < 5; i++) {
            adapter.storeCustomer(
                    CustomerTestFixture.createValidPersonalCustomerWithDocumentNumber(
                            String.format("1234567%d", i)
                    )
            ).block();
        }

        adapter.getCustomersPaginated(1, 2, null)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getContent()).hasSize(2);
                    assertThat(result.getTotalElements()).isEqualTo(5);
                    assertThat(result.getTotalPages()).isEqualTo(3);
                    assertThat(result.isLast()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should paginate customers with type filter")
    void shouldPaginateCustomersWithFilter() {
        var adapter = getBean(CustomerRepositoryAdapter.class);

        adapter.storeCustomer(CustomerTestFixture.createValidPersonalCustomer())
                .then(adapter.storeCustomer(CustomerTestFixture.createValidBusinessCustomer()))
                .then(adapter.getCustomersPaginated(1, 10, CustomerType.PERSONAL))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getContent()).hasSize(1);
                    assertThat(result.getContent().get(0).getCustomerType())
                            .isEqualTo(CustomerType.PERSONAL);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle empty pagination")
    void shouldHandleEmptyPagination() {
        var adapter = getBean(CustomerRepositoryAdapter.class);

        adapter.getCustomersPaginated(1, 10, null)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getContent()).isEmpty();
                    assertThat(result.getTotalElements()).isEqualTo(0);
                })
                .verifyComplete();
    }
}
