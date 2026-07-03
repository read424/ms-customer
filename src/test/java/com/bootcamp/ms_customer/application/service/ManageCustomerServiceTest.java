package com.bootcamp.ms_customer.application.service;

import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.CustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Service Tests")
class ManageCustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @InjectMocks
    private ManageCustomerService customerService;

    @Test
    @DisplayName("Should create customer successfully")
    void testCreateCustomerSuccess() {
        CreateCustomerDto createDto = new CreateCustomerDto();
        createDto.setFirstName("Juan");
        createDto.setLastName("Perez");
        createDto.setDocumentType(DocumentType.CEDULA_IDENTIDAD);
        createDto.setDocumentNumber("12345678");
        createDto.setEmail("juan@example.com");
        createDto.setPhoneNumber("+34912345678");
        createDto.setCustomerType(CustomerType.INDIVIDUAL);
        createDto.setStatus(CustomerStatus.ACTIVE);

        CustomerDto expectedCustomer = new CustomerDto();
        expectedCustomer.setCustomerId(UUID.randomUUID().toString());
        expectedCustomer.setFirstName("Juan");
        expectedCustomer.setLastName("Perez");
        expectedCustomer.setStatus(CustomerStatus.ACTIVE);
        expectedCustomer.setCreatedAt(LocalDateTime.now());

        when(customerRepository.save(any())).thenReturn(Mono.just(expectedCustomer));

        customerService.createCustomer(createDto)
                .as(StepVerifier::create)
                .assertNext(savedCustomer -> {
                    assertThat(savedCustomer).isNotNull();
                    assertThat(savedCustomer.getFirstName()).isEqualTo("Juan");
                    assertThat(savedCustomer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find customer by ID successfully")
    void testFindCustomerByIdSuccess() {
        String customerId = UUID.randomUUID().toString();
        CustomerDto expectedCustomer = new CustomerDto();
        expectedCustomer.setCustomerId(customerId);
        expectedCustomer.setFirstName("Maria");
        expectedCustomer.setLastName("Garcia");
        expectedCustomer.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(customerId)).thenReturn(Mono.just(expectedCustomer));

        customerService.findCustomerById(customerId)
                .as(StepVerifier::create)
                .assertNext(customer -> {
                    assertThat(customer).isNotNull();
                    assertThat(customer.getCustomerId()).isEqualTo(customerId);
                    assertThat(customer.getFirstName()).isEqualTo("Maria");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void testFindCustomerByIdNotFound() {
        String customerId = "non-existent-id";

        when(customerRepository.findById(customerId))
                .thenReturn(Mono.error(new CustomerNotFoundException("Customer not found")));

        customerService.findCustomerById(customerId)
                .as(StepVerifier::create)
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should update customer successfully")
    void testUpdateCustomerSuccess() {
        String customerId = UUID.randomUUID().toString();
        CreateCustomerDto updateDto = new CreateCustomerDto();
        updateDto.setFirstName("Juan Carlos");
        updateDto.setLastName("Perez Garcia");

        CustomerDto updatedCustomer = new CustomerDto();
        updatedCustomer.setCustomerId(customerId);
        updatedCustomer.setFirstName("Juan Carlos");
        updatedCustomer.setLastName("Perez Garcia");
        updatedCustomer.setStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(customerId)).thenReturn(Mono.just(updatedCustomer));
        when(customerRepository.save(any())).thenReturn(Mono.just(updatedCustomer));

        customerService.updateCustomer(customerId, updateDto)
                .as(StepVerifier::create)
                .assertNext(customer -> {
                    assertThat(customer).isNotNull();
                    assertThat(customer.getFirstName()).isEqualTo("Juan Carlos");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void testDeleteCustomerSuccess() {
        String customerId = UUID.randomUUID().toString();

        when(customerRepository.deleteById(customerId)).thenReturn(Mono.empty());

        customerService.deleteCustomer(customerId)
                .as(StepVerifier::create)
                .expectComplete()
                .verify();
    }
}
