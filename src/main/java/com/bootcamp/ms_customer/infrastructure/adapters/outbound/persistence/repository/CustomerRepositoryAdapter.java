package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository;

import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper.CustomerPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final SpringDataCustomerRepository springDataRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Mono<Customer> save(Customer customer) {
        return springDataRepository.save(customerPersistenceMapper.toEntity(customer))
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Customer> findById(String customerId) {
        return springDataRepository.findById(customerId)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Customer> findAll() {
        return springDataRepository.findAll()
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String customerId) {
        return springDataRepository.deleteById(customerId);
    }

    @Override
    public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
        return springDataRepository.existsByDocumentNumber(documentNumber);
    }
}
