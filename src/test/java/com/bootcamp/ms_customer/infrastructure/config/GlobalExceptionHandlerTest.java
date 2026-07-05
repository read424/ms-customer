package com.bootcamp.ms_customer.infrastructure.config;

import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServerWebExchange mockExchange;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockExchange = createMockExchange("/api/v1/customers");
    }

    @Test
    @DisplayName("should handle CustomerNotFoundException with 404 status")
    void shouldHandleCustomerNotFoundException() {
        var exception = new CustomerNotFoundException("Customer not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFoundException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Cliente No Encontrado", body.getError());
        assertEquals("Customer not found", body.getMessage());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("should handle InvalidCustomerDataException with 400 status")
    void shouldHandleInvalidCustomerDataException() {
        var exception = new InvalidCustomerDataException("Invalid email format");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCustomerDataException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Datos Inválidos", body.getError());
        assertEquals("Invalid email format", body.getMessage());
    }

    @Test
    @DisplayName("should handle ServerWebInputException with cause message")
    void shouldHandleServerWebInputExceptionWithCause() {
        var cause = new IllegalArgumentException("Invalid request body");
        var exception = new ServerWebInputException("Input error", null, cause);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServerWebInputException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Petición Incorrecta", body.getError());
        assertEquals("Invalid request body", body.getMessage());
    }

    @Test
    @DisplayName("should handle ServerWebInputException without cause")
    void shouldHandleServerWebInputExceptionWithoutCause() {
        var exception = new ServerWebInputException("Input error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServerWebInputException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Input error", body.getMessage());
    }

    @Test
    @DisplayName("should handle DecodingException with 400 status")
    void shouldHandleDecodingException() {
        var exception = new DecodingException("Invalid JSON");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDecodingException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Error de Decodificación JSON", body.getError());
        assertEquals("Invalid JSON", body.getMessage());
    }

    @Test
    @DisplayName("should handle generic Exception with 500 status")
    void shouldHandleGenericException() {
        var exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(
                exception, mockExchange);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("Error Interno del Servidor", body.getError());
        assertEquals("Ha ocurrido un error inesperado", body.getMessage());
    }

    @Test
    @DisplayName("should verify error response has required fields")
    void shouldVerifyErrorResponseStructure() {
        var exception = new CustomerNotFoundException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFoundException(
                exception, mockExchange);

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getTimestamp());
        assertNotNull(body.getStatus());
        assertNotNull(body.getError());
        assertNotNull(body.getMessage());
        assertNotNull(body.getPath());
    }

    private ServerWebExchange createMockExchange(String path) {
        var exchange = mock(ServerWebExchange.class);
        var pathContainer = mock(PathContainer.class);

        when(pathContainer.value()).thenReturn(path);

        var request = mock(Object.class);
        when(exchange.getRequest()).thenReturn(request);

        return exchange;
    }
}
