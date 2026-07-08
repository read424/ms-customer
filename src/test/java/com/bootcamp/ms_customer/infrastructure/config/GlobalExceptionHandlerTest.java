package com.bootcamp.ms_customer.infrastructure.config;

import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("should handle CustomerNotFoundException with 404 status")
    void shouldHandleCustomerNotFoundExceptionWith404() {
        ServerWebExchange exchange = mockExchange("/api/v1/customers/123");
        CustomerNotFoundException ex = new CustomerNotFoundException("Customer not found");

        ResponseEntity<ErrorResponse> response = handler.handleCustomerNotFoundException(ex, exchange);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Cliente No Encontrado", response.getBody().getError());
    }

    @Test
    @DisplayName("should handle InvalidCustomerDataException with 400 status")
    void shouldHandleInvalidCustomerDataExceptionWith400() {
        ServerWebExchange exchange = mockExchange("/api/v1/customers");
        InvalidCustomerDataException ex = new InvalidCustomerDataException("Invalid data");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCustomerDataException(ex, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Datos Inválidos", response.getBody().getError());
    }

    @Test
    @DisplayName("should handle NoResourceFoundException for favicon.ico silently")
    void shouldHandleNoResourceFoundExceptionForFaviconIcoSilently() {
        ServerWebExchange exchange = mockExchange("/favicon.ico");
        NoResourceFoundException ex = new NoResourceFoundException("favicon.ico not found");

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(ex, exchange);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("should handle NoResourceFoundException for other resources with error response")
    void shouldHandleNoResourceFoundExceptionForOtherResourcesWithErrorResponse() {
        ServerWebExchange exchange = mockExchange("/api/v1/unknown");
        NoResourceFoundException ex = new NoResourceFoundException("Resource not found");

        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFoundException(ex, exchange);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("No Encontrado", response.getBody().getError());
    }

    @Test
    @DisplayName("should handle DecodingException with 400 status")
    void shouldHandleDecodingExceptionWith400() {
        ServerWebExchange exchange = mockExchange("/api/v1/customers");
        DecodingException ex = new DecodingException("Invalid JSON");

        ResponseEntity<ErrorResponse> response = handler.handleDecodingException(ex, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("should handle generic Exception with 500 status")
    void shouldHandleGenericExceptionWith500() {
        ServerWebExchange exchange = mockExchange("/api/v1/customers");
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, exchange);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error Interno del Servidor", response.getBody().getError());
    }

    private ServerWebExchange mockExchange(String path) {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        var request = mock(org.springframework.http.server.reactive.ServerHttpRequest.class);
        var requestPath = mock(org.springframework.http.server.RequestPath.class);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn(path);

        return exchange;
    }
}
