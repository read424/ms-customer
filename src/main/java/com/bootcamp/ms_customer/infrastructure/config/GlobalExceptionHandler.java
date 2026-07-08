package com.bootcamp.ms_customer.infrastructure.config;

import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex,
            ServerWebExchange exchange) {
        log.warn("Cliente no encontrado: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, "Cliente No Encontrado", exchange);
    }

    @ExceptionHandler(InvalidCustomerDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCustomerDataException(
            InvalidCustomerDataException ex,
            ServerWebExchange exchange) {
        log.warn("Datos inválidos del cliente: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "Datos Inválidos", exchange);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleServerWebInputException(
            ServerWebInputException ex,
            ServerWebExchange exchange) {
        log.warn("Error de entrada de datos: {}", ex.getMessage());
        String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST, "Petición Incorrecta", exchange);
    }

    @ExceptionHandler(DecodingException.class)
    public ResponseEntity<ErrorResponse> handleDecodingException(
            DecodingException ex,
            ServerWebExchange exchange) {
        log.warn("Error de decodificación JSON: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, "Error de Decodificación JSON", exchange);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            ServerWebExchange exchange) {
        if (exchange.getRequest().getPath().value().contains("favicon.ico")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildErrorResponse("Recurso no encontrado", HttpStatus.NOT_FOUND, "No Encontrado", exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {
        log.error("Error interno del servidor", ex);
        return buildErrorResponse("Ha ocurrido un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR,
                "Error Interno del Servidor", exchange);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status,
            String error, ServerWebExchange exchange) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
                .status(status.value())
                .error(error)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
