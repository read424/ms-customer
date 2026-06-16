package com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.controller;

import com.bootcamp.ms_customer.application.ports.input.CustomerServicePort;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CreateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.UpdateCustomerMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.mapper.CustomerResponseMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.request.CreateCustomerRequest;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.request.UpdateCustomerRequest;
import com.bootcamp.ms_customer.infrastructure.adapters.inbound.rest.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Operaciones CRUD para gestión de clientes")
public class CustomerController {

    private final CustomerServicePort customerService;
    private final CreateCustomerMapper createCustomerMapper;
    private final UpdateCustomerMapper updateCustomerMapper;
    private final CustomerResponseMapper customerResponseMapper;

    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Registra un nuevo cliente en el sistema")
    @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o documento duplicado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public Mono<ResponseEntity<CustomerResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        return customerService
                .createCustomer(createCustomerMapper.toDomainDto(request))
                .map(customerResponseMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Retorna lista de todos los clientes registrados")
    @ApiResponse(responseCode = "200", description = "Clientes obtenidos exitosamente")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public Mono<ResponseEntity<Flux<CustomerResponse>>> findAll() {
        Flux<CustomerResponse> customers = customerService.findAllCustomers()
                .map(customerResponseMapper::toResponse);
        return Mono.just(ResponseEntity.ok(customers));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna los datos de un cliente específico")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public Mono<ResponseEntity<CustomerResponse>> findById(
            @PathVariable String customerId) {
        return customerService.findCustomerById(customerId)
                .map(customerResponseMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza datos de un cliente existente")
    @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public Mono<ResponseEntity<CustomerResponse>> update(
            @PathVariable String customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return customerService
                .updateCustomer(customerId, updateCustomerMapper.toDomainDto(request))
                .map(customerResponseMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente del sistema")
    @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    public Mono<ResponseEntity<Void>> delete(
            @PathVariable String customerId) {
        return customerService.deleteCustomer(customerId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
