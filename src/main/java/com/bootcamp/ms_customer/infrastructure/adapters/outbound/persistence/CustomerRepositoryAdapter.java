package com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.bootcamp.ms_customer.application.ports.output.CustomerRepositoryPort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.entity.CustomerEntity;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.mapper.CustomerPersistenceMapper;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.persistence.repository.SpringDataCustomerRepository;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final SpringDataCustomerRepository springDataRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Mono<Customer> storeCustomer(Customer customer) {
        return springDataRepository.save(customerPersistenceMapper.toEntity(customer))
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Customer> getCustomerById(String customerId) {
        return springDataRepository.findById(customerId)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Customer> getAllCustomers() {
        return springDataRepository.findAll()
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Void> removeCustomerById(String customerId) {
        return springDataRepository.deleteById(customerId);
    }

    @Override
    public Mono<Boolean> hasCustomerWithDocumentNumber(String documentNumber) {
        return springDataRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public Mono<PaginatedResult<Customer>> getCustomersPaginated(
            int page, int size, CustomerType type) {
        
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        
        Flux<CustomerEntity> entityFlux;
        Mono<Long> countMono;
        
        if (type == null) {
            entityFlux = springDataRepository.findAllBy(pageRequest);
            countMono = springDataRepository.count();
        } else {
            entityFlux = springDataRepository.findByCustomerType(type, pageRequest);
            countMono = springDataRepository.countByCustomerType(type);
        }
        
        return entityFlux
                .map(customerPersistenceMapper::toDomain)
                .collectList()
                .zipWith(countMono)
                .map(tuple -> {
                    List<Customer> list = tuple.getT1();
                    long totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / size);
                    boolean isLast = (totalPages == 0) || (page >= totalPages);
                    
                    return PaginatedResult.<Customer>builder()
                            .content(list)
                            .pageNumber(page)
                            .pageSize(size)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .isLast(isLast)
                            .build();
                });
    }
}
