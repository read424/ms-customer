package com.bootcamp.ms_customer.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@DisplayName("Tests para GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServerWebExchange exchange;
    private ServerHttpRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        exchange = Mockito.mock(ServerWebExchange.class);
        request = Mockito.mock(ServerHttpRequest.class);
        when(exchange.getRequest()).thenReturn(request);

        RequestPath requestPath = Mockito.mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/v1/customers");
        when(request.getPath()).thenReturn(requestPath);
    }

    @Test
    @DisplayName("Debe manejar ServerWebInputException y retornar 400 Bad Request")
    void handleServerWebInputException() {
        ServerWebInputException exception = new ServerWebInputException("Error de entrada de datos", null, new IllegalArgumentException("Unexpected value 'C.I.'"));

        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleServerWebInputException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Petición Incorrecta", body.getError());
        assertEquals("Unexpected value 'C.I.'", body.getMessage());
        assertEquals("/api/v1/customers", body.getPath());
    }

    @Test
    @DisplayName("Debe manejar DecodingException y retornar 400 Bad Request")
    void handleDecodingException() {
        DecodingException exception = new DecodingException("Error de decodificación JSON: Cannot construct instance of DocumentType");

        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleDecodingException(exception, exchange);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(400, body.getStatus());
        assertEquals("Error de Decodificación JSON", body.getError());
        assertEquals("Error de decodificación JSON: Cannot construct instance of DocumentType", body.getMessage());
        assertEquals("/api/v1/customers", body.getPath());
    }
}
