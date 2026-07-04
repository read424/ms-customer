package com.bootcamp.ms_customer.domain.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Model Unit Tests")
class DomainModelTest {

    // ──────────────────────────────────────────────────────────────────
    // PaginatedResult DTO Tests
    // ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PaginatedResult DTO Tests")
    class PaginatedResultTest {

        @Test
        @DisplayName("should create PaginatedResult using builder")
        void shouldCreatePaginatedResultWithBuilder() {
            var customer = Customer.builder()
                    .customerId("CUST-001")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            var result = PaginatedResult.<Customer>builder()
                    .content(Arrays.asList(customer))
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(100)
                    .totalPages(10)
                    .isLast(false)
                    .build();

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getPageNumber());
            assertEquals(10, result.getPageSize());
            assertEquals(100, result.getTotalElements());
            assertEquals(10, result.getTotalPages());
            assertFalse(result.isLast());
        }

        @Test
        @DisplayName("should create PaginatedResult using no-args constructor")
        void shouldCreatePaginatedResultWithNoArgsConstructor() {
            var result = new PaginatedResult<String>();
            assertNull(result.getContent());
            assertEquals(0, result.getPageNumber());
        }

        @Test
        @DisplayName("should create PaginatedResult using all-args constructor")
        void shouldCreatePaginatedResultWithAllArgsConstructor() {
            List<String> content = Arrays.asList("item1", "item2");
            var result = new PaginatedResult<>(content, 1, 10, 2, 1, true);

            assertEquals(2, result.getContent().size());
            assertEquals(1, result.getPageNumber());
            assertEquals(10, result.getPageSize());
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertTrue(result.isLast());
        }

        @Test
        @DisplayName("should set and get fields via setters")
        void shouldSetAndGetFieldsViaSets() {
            var result = new PaginatedResult<Integer>();
            result.setContent(Arrays.asList(1, 2, 3));
            result.setPageNumber(2);
            result.setPageSize(20);
            result.setTotalElements(150);
            result.setTotalPages(8);
            result.setLast(true);

            assertEquals(3, result.getContent().size());
            assertEquals(2, result.getPageNumber());
            assertEquals(20, result.getPageSize());
            assertEquals(150, result.getTotalElements());
            assertEquals(8, result.getTotalPages());
            assertTrue(result.isLast());
        }

        @Test
        @DisplayName("should support equality")
        void shouldSupportEquality() {
            var result1 = PaginatedResult.<String>builder()
                    .content(Arrays.asList("A"))
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .isLast(true)
                    .build();

            var result2 = PaginatedResult.<String>builder()
                    .content(Arrays.asList("A"))
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .isLast(true)
                    .build();

            assertEquals(result1, result2);
            assertEquals(result1.hashCode(), result2.hashCode());
        }

        @Test
        @DisplayName("should support inequality")
        void shouldSupportInequality() {
            var result1 = PaginatedResult.<String>builder()
                    .pageNumber(1).pageSize(10)
                    .build();

            var result2 = PaginatedResult.<String>builder()
                    .pageNumber(2).pageSize(10)
                    .build();

            assertNotEquals(result1, result2);
        }

        @Test
        @DisplayName("should be serializable")
        void shouldBeSerializable() {
            var result = PaginatedResult.<String>builder()
                    .content(Arrays.asList("test"))
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .isLast(true)
                    .build();

            assertNotNull(result);
            assertTrue(result instanceof java.io.Serializable);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // CreateCustomerDto Tests
    // ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CreateCustomerDto Tests")
    class CreateCustomerDtoTest {

        @Test
        @DisplayName("should create CreateCustomerDto with all fields")
        void shouldCreateCreateCustomerDtoWithAllFields() {
            var dto = new CreateCustomerDto();
            dto.setFirstName("John");
            dto.setLastName("Doe");
            dto.setDocumentNumber("12345678A");
            dto.setDocumentType(DocumentType.DNI);
            dto.setCustomerType(CustomerType.PERSONAL);
            dto.setEmail("john@example.com");
            dto.setPhoneNumber("+34912345678");
            dto.setBusinessName(null);

            assertEquals("John", dto.getFirstName());
            assertEquals("Doe", dto.getLastName());
            assertEquals("12345678A", dto.getDocumentNumber());
            assertEquals(DocumentType.DNI, dto.getDocumentType());
            assertEquals(CustomerType.PERSONAL, dto.getCustomerType());
            assertEquals("john@example.com", dto.getEmail());
            assertEquals("+34912345678", dto.getPhoneNumber());
            assertNull(dto.getBusinessName());
        }

        @Test
        @DisplayName("should create PERSONAL customer DTO")
        void shouldCreatePersonalCustomerDto() {
            var dto = new CreateCustomerDto();
            dto.setFirstName("John");
            dto.setLastName("Doe");
            dto.setDocumentNumber("12345678A");
            dto.setDocumentType(DocumentType.DNI);
            dto.setCustomerType(CustomerType.PERSONAL);

            assertEquals(CustomerType.PERSONAL, dto.getCustomerType());
            assertNotNull(dto.getFirstName());
            assertNotNull(dto.getLastName());
        }

        @Test
        @DisplayName("should create BUSINESS customer DTO")
        void shouldCreateBusinessCustomerDto() {
            var dto = new CreateCustomerDto();
            dto.setBusinessName("Acme Corp");
            dto.setDocumentNumber("ABC123");
            dto.setDocumentType(DocumentType.RUC);
            dto.setCustomerType(CustomerType.BUSINESS);

            assertEquals(CustomerType.BUSINESS, dto.getCustomerType());
            assertEquals("Acme Corp", dto.getBusinessName());
        }

        @Test
        @DisplayName("should support equality")
        void shouldSupportEquality() {
            var dto1 = new CreateCustomerDto();
            dto1.setFirstName("John");
            dto1.setDocumentNumber("12345678A");

            var dto2 = new CreateCustomerDto();
            dto2.setFirstName("John");
            dto2.setDocumentNumber("12345678A");

            assertEquals(dto1, dto2);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // UpdateCustomerDto Tests
    // ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("UpdateCustomerDto Tests")
    class UpdateCustomerDtoTest {

        @Test
        @DisplayName("should create UpdateCustomerDto with partial fields")
        void shouldCreateUpdateCustomerDtoWithPartialFields() {
            var dto = new UpdateCustomerDto();
            dto.setFirstName("Jane");
            dto.setEmail("jane@example.com");

            assertEquals("Jane", dto.getFirstName());
            assertEquals("jane@example.com", dto.getEmail());
            assertNull(dto.getLastName());
            assertNull(dto.getDocumentNumber());
        }

        @Test
        @DisplayName("should allow all fields to be null")
        void shouldAllowAllFieldsToBeNull() {
            var dto = new UpdateCustomerDto();

            assertNull(dto.getFirstName());
            assertNull(dto.getLastName());
            assertNull(dto.getDocumentNumber());
            assertNull(dto.getEmail());
            assertNull(dto.getPhoneNumber());
            assertNull(dto.getBusinessName());
        }

        @Test
        @DisplayName("should update only email and phone")
        void shouldUpdateOnlyEmailAndPhone() {
            var dto = new UpdateCustomerDto();
            dto.setEmail("newemail@example.com");
            dto.setPhoneNumber("+34987654321");

            assertEquals("newemail@example.com", dto.getEmail());
            assertEquals("+34987654321", dto.getPhoneNumber());
            assertNull(dto.getFirstName());
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Enum Tests
    // ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CustomerType Enum Tests")
    class CustomerTypeTest {

        @Test
        @DisplayName("should have PERSONAL and BUSINESS values")
        void shouldHavePersonalAndBusinessValues() {
            assertEquals(2, CustomerType.values().length);
            assertTrue(Arrays.asList(CustomerType.values()).contains(CustomerType.PERSONAL));
            assertTrue(Arrays.asList(CustomerType.values()).contains(CustomerType.BUSINESS));
        }

        @Test
        @DisplayName("should convert string to CustomerType")
        void shouldConvertStringToCustomerType() {
            assertEquals(CustomerType.PERSONAL, CustomerType.valueOf("PERSONAL"));
            assertEquals(CustomerType.BUSINESS, CustomerType.valueOf("BUSINESS"));
        }

        @Test
        @DisplayName("should throw exception for invalid value")
        void shouldThrowExceptionForInvalidValue() {
            assertThrows(IllegalArgumentException.class, () -> CustomerType.valueOf("INVALID"));
        }
    }

    @Nested
    @DisplayName("CustomerStatus Enum Tests")
    class CustomerStatusTest {

        @Test
        @DisplayName("should have ACTIVE and INACTIVE values")
        void shouldHaveActiveAndInactiveValues() {
            assertEquals(2, CustomerStatus.values().length);
            assertTrue(Arrays.asList(CustomerStatus.values()).contains(CustomerStatus.ACTIVE));
            assertTrue(Arrays.asList(CustomerStatus.values()).contains(CustomerStatus.INACTIVE));
        }

        @Test
        @DisplayName("should convert string to CustomerStatus")
        void shouldConvertStringToCustomerStatus() {
            assertEquals(CustomerStatus.ACTIVE, CustomerStatus.valueOf("ACTIVE"));
            assertEquals(CustomerStatus.INACTIVE, CustomerStatus.valueOf("INACTIVE"));
        }
    }

    @Nested
    @DisplayName("DocumentType Enum Tests")
    class DocumentTypeTest {

        @Test
        @DisplayName("should have all document type values")
        void shouldHaveAllDocumentTypes() {
            var values = DocumentType.values();
            assertTrue(values.length >= 4);
            assertTrue(Arrays.asList(values).contains(DocumentType.DNI));
            assertTrue(Arrays.asList(values).contains(DocumentType.CE));
            assertTrue(Arrays.asList(values).contains(DocumentType.PASSPORT));
            assertTrue(Arrays.asList(values).contains(DocumentType.RUC));
        }

        @Test
        @DisplayName("should convert string to DocumentType")
        void shouldConvertStringToDocumentType() {
            assertEquals(DocumentType.DNI, DocumentType.valueOf("DNI"));
            assertEquals(DocumentType.CE, DocumentType.valueOf("CE"));
            assertEquals(DocumentType.PASSPORT, DocumentType.valueOf("PASSPORT"));
            assertEquals(DocumentType.RUC, DocumentType.valueOf("RUC"));
        }

        @Test
        @DisplayName("should be comparable")
        void shouldBeComparable() {
            assertNotEquals(DocumentType.DNI, DocumentType.PASSPORT);
            assertEquals(DocumentType.DNI, DocumentType.DNI);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Exception Tests
    // ──────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("InvalidCustomerDataException Tests")
    class InvalidCustomerDataExceptionTest {

        @Test
        @DisplayName("should throw exception with message")
        void shouldThrowExceptionWithMessage() {
            var message = "Invalid customer data";
            var exception = assertThrows(InvalidCustomerDataException.class, () -> {
                throw new InvalidCustomerDataException(message);
            });

            assertEquals(message, exception.getMessage());
        }

        @Test
        @DisplayName("should throw exception with message and cause")
        void shouldThrowExceptionWithMessageAndCause() {
            var message = "Invalid customer data";
            var cause = new RuntimeException("Underlying cause");

            var exception = assertThrows(InvalidCustomerDataException.class, () -> {
                throw new InvalidCustomerDataException(message, cause);
            });

            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            var exception = new InvalidCustomerDataException("Test");
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("should preserve message across toString")
        void shouldPreserveMessageAcrossToString() {
            var message = "Customer validation failed";
            var exception = new InvalidCustomerDataException(message);

            assertTrue(exception.toString().contains("InvalidCustomerDataException"));
            assertEquals(message, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("CustomerNotFoundException Tests")
    class CustomerNotFoundExceptionTest {

        @Test
        @DisplayName("should throw exception with customerId message")
        void shouldThrowExceptionWithCustomerIdMessage() {
            var customerId = "CUST-999";
            var exception = assertThrows(CustomerNotFoundException.class, () -> {
                throw new CustomerNotFoundException(customerId);
            });

            assertTrue(exception.getMessage().contains(customerId));
        }

        @Test
        @DisplayName("should throw exception with message and cause")
        void shouldThrowExceptionWithMessageAndCause() {
            var message = "Customer not found";
            var cause = new Exception("DB error");

            var exception = assertThrows(CustomerNotFoundException.class, () -> {
                throw new CustomerNotFoundException(message, cause);
            });

            assertEquals(message, exception.getMessage());
            assertEquals(cause, exception.getCause());
        }

        @Test
        @DisplayName("should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            var exception = new CustomerNotFoundException("CUST-001");
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("should work with various customer ID formats")
        void shouldWorkWithVariousCustomerIdFormats() {
            var uuid = "550e8400-e29b-41d4-a716-446655440000";
            var exception1 = new CustomerNotFoundException(uuid);
            assertTrue(exception1.getMessage().contains(uuid));

            var legacyId = "CUST-12345";
            var exception2 = new CustomerNotFoundException(legacyId);
            assertTrue(exception2.getMessage().contains(legacyId));
        }
    }
}
