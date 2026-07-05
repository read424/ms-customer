package com.bootcamp.ms_customer.domain.mapper;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {CustomerDomainMapperImpl.class})
@DisplayName("Customer Domain Mapper Tests")
class CustomerDomainMapperTest {

    @Autowired
    private CustomerDomainMapper customerDomainMapper;

    @Test
    @DisplayName("should map CreateCustomerDto to Customer")
    void shouldMapCreateCustomerDtoToCustomer() {
        var createDto = new CreateCustomerDto();
        createDto.setFirstName("John");
        createDto.setLastName("Doe");
        createDto.setDocumentType(DocumentType.DNI);
        createDto.setDocumentNumber("12345678A");
        createDto.setEmail("john@example.com");
        createDto.setCustomerType(CustomerType.PERSONAL);

        Customer result = customerDomainMapper.toCustomer(createDto);

        assertNotNull(result);
        assertNotNull(result.getCustomerId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("12345678A", result.getDocumentNumber());
        assertEquals(CustomerType.PERSONAL, result.getCustomerType());
        assertNull(result.getStatus());
    }

    @Test
    @DisplayName("should generate unique UUIDs for each mapping")
    void shouldGenerateUniqueUUIDs() {
        var dto1 = new CreateCustomerDto();
        dto1.setFirstName("John");
        dto1.setCustomerType(CustomerType.PERSONAL);

        var dto2 = new CreateCustomerDto();
        dto2.setFirstName("Jane");
        dto2.setCustomerType(CustomerType.PERSONAL);

        Customer result1 = customerDomainMapper.toCustomer(dto1);
        Customer result2 = customerDomainMapper.toCustomer(dto2);

        assertNotEquals(result1.getCustomerId(), result2.getCustomerId());
    }

    @Test
    @DisplayName("should map business customer")
    void shouldMapBusinessCustomer() {
        var createDto = new CreateCustomerDto();
        createDto.setBusinessName("Acme Corp");
        createDto.setDocumentType(DocumentType.RUC);
        createDto.setDocumentNumber("A12345678");
        createDto.setCustomerType(CustomerType.BUSINESS);

        Customer result = customerDomainMapper.toCustomer(createDto);

        assertNotNull(result);
        assertEquals("Acme Corp", result.getBusinessName());
        assertEquals(CustomerType.BUSINESS, result.getCustomerType());
    }
}
