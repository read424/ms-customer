package com.bootcamp.ms_customer.infrastructure.test.persistence.fixtures;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerTestFixture {

    public static Customer createValidPersonalCustomer() {
        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("John")
                .lastName("Doe")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678A")
                .email("john.doe@example.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.PERSONAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Customer createValidPersonalCustomerWithDocumentNumber(String documentNumber) {
        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("John")
                .lastName("Doe")
                .documentType(DocumentType.DNI)
                .documentNumber(documentNumber)
                .email("john.doe@example.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.PERSONAL)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Customer createValidBusinessCustomer() {
        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .businessName("Acme Corp")
                .documentType(DocumentType.RUC)
                .documentNumber("A12345678")
                .email("contact@acme.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.BUSINESS)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Customer createValidBusinessCustomerWithDocumentNumber(String documentNumber) {
        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .businessName("Acme Corp")
                .documentType(DocumentType.RUC)
                .documentNumber(documentNumber)
                .email("contact@acme.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.BUSINESS)
                .status(CustomerStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Customer createInactivePersonalCustomer() {
        return Customer.builder()
                .customerId(UUID.randomUUID().toString())
                .firstName("Jane")
                .lastName("Smith")
                .documentType(DocumentType.DNI)
                .documentNumber("87654321B")
                .email("jane.smith@example.com")
                .phoneNumber("+34912345679")
                .customerType(CustomerType.PERSONAL)
                .status(CustomerStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private CustomerTestFixture() {
    }
}
