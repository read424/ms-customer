package com.bootcamp.ms_customer.domain.mapper;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Customer Domain Mapper Tests")
class CustomerDomainMapperTest {

    @Autowired
    private CustomerDomainMapper customerDomainMapper;

    @Test
    @DisplayName("should map CreateCustomerDto to Customer with generated UUID")
    void shouldMapCreateCustomerDtoToCustomer() {
        var createDto = new CreateCustomerDto();
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setDocumentType(DocumentType.DNI);
        createDto.setDocumentNumber("12345678A");
        createDto.setEmail("john@example.com");
        createDto.setPhoneNumber("+34912345678");
        createDto.setCustomerType(CustomerType.PERSONAL);

        Customer result = customerDomainMapper.toCustomer(createDto);

        assertNotNull(result);
        assertNotNull(result.getCustomerId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("12345678A", result.getDocumentNumber());
        assertEquals(DocumentType.DNI, result.getDocumentType());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("+34912345678", result.getPhoneNumber());
        assertEquals(CustomerType.PERSONAL, result.getCustomerType());
        assertNull(result.getStatus());
    }

    @Test
    @DisplayName("should generate unique UUIDs for each mapping")
    void shouldGenerateUniqueUUIDs() {
        var createDto1 = new CreateCustomerDto();
        createDto1.setFirstName("John");
        createDto1.setLastName("Doe");
        createDto1.setDocumentType(DocumentType.DNI);
        createDto1.setDocumentNumber("12345678A");
        createDto1.setCustomerType(CustomerType.PERSONAL);

        var createDto2 = new CreateCustomerDto();
        createDto2.setFirstName("Jane");
        createDto2.setLastName("Smith");
        createDto2.setDocumentType(DocumentType.DNI);
        createDto2.setDocumentNumber("87654321B");
        createDto2.setCustomerType(CustomerType.PERSONAL);

        Customer result1 = customerDomainMapper.toCustomer(createDto1);
        Customer result2 = customerDomainMapper.toCustomer(createDto2);

        assertNotEquals(result1.getCustomerId(), result2.getCustomerId());
    }

    @Test
    @DisplayName("should map business customer successfully")
    void shouldMapBusinessCustomer() {
        var createDto = new CreateCustomerDto();
        createDto.setBusinessName("Acme Corp");
        createDto.setDocumentType(DocumentType.RUC);
        createDto.setDocumentNumber("A12345678");
        createDto.setEmail("contact@acme.com");
        createDto.setPhoneNumber("+34912345678");
        createDto.setCustomerType(CustomerType.BUSINESS);

        Customer result = customerDomainMapper.toCustomer(createDto);

        assertNotNull(result);
        assertEquals("Acme Corp", result.getBusinessName());
        assertEquals(DocumentType.RUC, result.getDocumentType());
        assertEquals("A12345678", result.getDocumentNumber());
        assertEquals(CustomerType.BUSINESS, result.getCustomerType());
        assertNull(result.getStatus());
    }

    @Test
    @DisplayName("should preserve all fields from CreateCustomerDto")
    void shouldPreserveAllFields() {
        var createDto = new CreateCustomerDto();
        createDto.setFirstName("Carlos");
        createDto.setLastName("García");
        createDto.setDocumentType(DocumentType.PASSPORT);
        createDto.setDocumentNumber("ES123456");
        createDto.setEmail("carlos@example.com");
        createDto.setPhoneNumber("+34654321987");
        createDto.setCustomerType(CustomerType.PERSONAL);

        Customer result = customerDomainMapper.toCustomer(createDto);

        assertEquals("Carlos", result.getFirstName());
        assertEquals("García", result.getLastName());
        assertEquals(DocumentType.PASSPORT, result.getDocumentType());
        assertEquals("ES123456", result.getDocumentNumber());
        assertEquals("carlos@example.com", result.getEmail());
        assertEquals("+34654321987", result.getPhoneNumber());
        assertEquals(CustomerType.PERSONAL, result.getCustomerType());
    }
}
