package com.bootcamp.ms_customer.domain.model.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super("Cliente con ID '" + customerId + "' no encontrado");
    }

    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
