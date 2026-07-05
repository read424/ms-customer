package com.bootcamp.ms_customer.infrastructure.config;

import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Global Exception Handler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServerWebExchange mockExchange;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockExchange = createMockExchange("/api/v1/customers");
    }

    @Test
    @DisplayName("should return 404 for CustomerNotFoundException")
    void shouldHandleCustomerNotFoundException() {
        var exception = new CustomerNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFoundException(exception, mockExchange);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Cliente No Encontrado", response.getBody().getError());
    }

    @Test
    @DisplayName("should return 400 for InvalidCustomerDataException")
    void shouldHandleInvalidCustomerDataException() {
        var exception = new InvalidCustomerDataException("Invalid");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidCustomerDataException(exception, mockExchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("should return 400 for ServerWebInputException")
    void shouldHandleServerWebInputException() {
        var exception = new ServerWebInputException("Error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServerWebInputException(exception, mockExchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("should return 400 for DecodingException")
    void shouldHandleDecodingException() {
        var exception = new DecodingException("Decode error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDecodingException(exception, mockExchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("should return 500 for generic Exception")
    void shouldHandleGenericException() {
        var exception = new RuntimeException("Error");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, mockExchange);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }

    private ServerWebExchange createMockExchange(String path) {
        var exchange = mock(ServerWebExchange.class);
        var request = mock(ServerHttpRequest.class);
        var requestPath = mock(RequestPath.class);

        when(requestPath.value()).thenReturn(path);
        when(request.getPath()).thenReturn(requestPath);
        when(exchange.getRequest()).thenReturn(request);

        return exchange;
    }
}
