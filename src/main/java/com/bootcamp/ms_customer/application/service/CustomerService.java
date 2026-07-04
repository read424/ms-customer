package com.bootcamp.ms_customer.application.service;

import com.bootcamp.ms_customer.domain.mapper.CustomerDomainMapper;
import com.bootcamp.ms_customer.application.ports.input.ManageCustomerUseCase;
import com.bootcamp.ms_customer.application.ports.output.CachePort;
import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.CreateCustomerDto;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.dto.UpdateCustomerDto;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.exception.CustomerNotFoundException;
import com.bootcamp.ms_customer.domain.model.exception.InvalidCustomerDataException;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService implements ManageCustomerUseCase {

    private final CustomerRepositoryPort customerRepositoryPort;
    private final CustomerDomainMapper customerDomainMapper;
    private final CachePort cachePort;
    private final SearchKeyGenerator searchKeyGenerator;

    @Override
    public Mono<Customer> createCustomer(CreateCustomerDto createDto) {
        Customer customer = customerDomainMapper.toCustomer(createDto);
        try {
            validateCustomerByType(customer);
        } catch (InvalidCustomerDataException e) {
            return Mono.error(e);
        }
        return checkDocumentNumberNotExists(customer.getDocumentNumber())
                .flatMap(notExists -> customerRepositoryPort.storeCustomer(customer))
                .flatMap(saved -> cachePort.invalidateAllCustomerListCaches().thenReturn(saved));
    }

    @Override
    public Mono<Customer> findCustomerById(String customerId) {
        return customerRepositoryPort.getCustomerById(customerId)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(customerId)));
    }

    @Override
    public Flux<Customer> findAllCustomers() {
        return customerRepositoryPort.getAllCustomers();
    }

    @Override
    public Mono<PaginatedResult<Customer>> findCustomers(int page, int size, CustomerType type) {
        String cacheKey = searchKeyGenerator.generateSearchKey(page, size, type);
        return cachePort.getCustomerListBySearchKey(cacheKey)
                .flatMap(cached -> {
                    if (cached.isPresent()) {
                        log.debug("Cache HIT para key: {}", cacheKey);
                        return Mono.just(cached.get());
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(
                        customerRepositoryPort.getCustomersPaginated(page, size, type)
                                .flatMap(result -> {
                                    log.debug("Cache MISS para key: {}. Almacenando en caché.", cacheKey);
                                    return cachePort.cacheCustomerListBySearchKey(cacheKey, result)
                                            .thenReturn(result);
                                })
                );
    }

    @Override
    public Mono<Customer> updateCustomer(String customerId, UpdateCustomerDto updateDto) {
        return findCustomerById(customerId)
                .flatMap(existingCustomer -> {
                    try {
                        validateCustomerIsActive(existingCustomer);
                        validateUpdateFields(existingCustomer, updateDto);
                    } catch (InvalidCustomerDataException e) {
                        return Mono.error(e);
                    }
                    return validateDocumentNumberUniquenessForUpdate(existingCustomer, updateDto)
                            .flatMap(unused -> {
                                applyUpdateFields(existingCustomer, updateDto);
                                return customerRepositoryPort.storeCustomer(existingCustomer);
                            });
                })
                .flatMap(updated -> cachePort.invalidateCustomerDetailCache(customerId)
                        .then(cachePort.invalidateAllCustomerListCaches())
                        .thenReturn(updated));
    }

    @Override
    public Mono<Void> deleteCustomer(String customerId) {
        return findCustomerById(customerId)
                .flatMap(customer -> customerRepositoryPort.removeCustomerById(customerId)
                        .then(cachePort.invalidateCustomerDetailCache(customerId))
                        .then(cachePort.invalidateAllCustomerListCaches()));
    }

    // ─── Validation Helpers ──────────────────────────────────────────────────
    private void validateCustomerByType(Customer customer) {
        if (customer.getCustomerType() == null) return;
        switch (customer.getCustomerType()) {
            case PERSONAL -> {
                if (isBlank(customer.getFirstName()) || isBlank(customer.getLastName())) {
                    throw new InvalidCustomerDataException(
                            "Para clientes de tipo PERSONAL, firstName y lastName son obligatorios");
                }
            }
            case BUSINESS -> {
                if (isBlank(customer.getBusinessName())) {
                    throw new InvalidCustomerDataException(
                            "Para clientes de tipo BUSINESS, businessName es obligatorio");
                }
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Mono<Boolean> checkDocumentNumberNotExists(String documentNumber) {
        return customerRepositoryPort.hasCustomerWithDocumentNumber(documentNumber)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidCustomerDataException(
                                "Ya existe un cliente con el número de documento: " + documentNumber));
                    }
                    return Mono.just(true);
                });
    }

    private void validateCustomerIsActive(Customer customer) {
        if (customer.getStatus() != null && !customer.getStatus().equals(com.bootcamp.ms_customer.domain.model.enums.CustomerStatus.ACTIVE)) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar un cliente con estado " + customer.getStatus() + ". El cliente debe estar ACTIVE");
        }
    }

    private void validateUpdateFields(Customer customer, UpdateCustomerDto updateDto) {
        if (customer.getCustomerType() == null) {
            throw new InvalidCustomerDataException("Tipo de cliente no definido");
        }

        switch (customer.getCustomerType()) {
            case PERSONAL -> validatePersonalCustomerUpdate(updateDto);
            case BUSINESS -> validateBusinessCustomerUpdate(updateDto);
        }
    }

    private void validatePersonalCustomerUpdate(UpdateCustomerDto updateDto) {
        // PERSONAL: permite actualizar documentNumber, firstName, lastName, email, phoneNumber
        // No permite actualizar businessName
        if (updateDto.getBusinessName() != null) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar 'businessName' en un cliente de tipo PERSONAL");
        }
    }

    private void validateBusinessCustomerUpdate(UpdateCustomerDto updateDto) {
        // BUSINESS: solo permite actualizar email y phoneNumber
        // No permite actualizar documentNumber, firstName, lastName, businessName
        if (updateDto.getDocumentNumber() != null) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar 'documentNumber' en un cliente de tipo BUSINESS");
        }
        if (updateDto.getFirstName() != null) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar 'firstName' en un cliente de tipo BUSINESS");
        }
        if (updateDto.getLastName() != null) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar 'lastName' en un cliente de tipo BUSINESS");
        }
        if (updateDto.getBusinessName() != null) {
            throw new InvalidCustomerDataException(
                    "No se puede actualizar 'businessName' después de la creación del cliente");
        }
    }

    private Mono<Boolean> validateDocumentNumberUniquenessForUpdate(Customer customer, UpdateCustomerDto updateDto) {
        if (updateDto.getDocumentNumber() == null || updateDto.getDocumentNumber().equals(customer.getDocumentNumber())) {
            return Mono.just(true);
        }

        return customerRepositoryPort.hasCustomerWithDocumentNumber(updateDto.getDocumentNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new InvalidCustomerDataException(
                                "Ya existe otro cliente con el número de documento: " + updateDto.getDocumentNumber()));
                    }
                    return Mono.just(true);
                });
    }

    private void applyUpdateFields(Customer customer, UpdateCustomerDto updateDto) {
        if (updateDto.getDocumentNumber() != null)  customer.setDocumentNumber(updateDto.getDocumentNumber());
        if (updateDto.getFirstName() != null)       customer.setFirstName(updateDto.getFirstName());
        if (updateDto.getLastName() != null)        customer.setLastName(updateDto.getLastName());
        if (updateDto.getEmail() != null)           customer.setEmail(updateDto.getEmail());
        if (updateDto.getPhoneNumber() != null)     customer.setPhoneNumber(updateDto.getPhoneNumber());
    }
}
