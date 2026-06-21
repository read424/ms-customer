package com.bootcamp.ms_customer.application.ports.output;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CachePort {

    Mono<Optional<PaginatedResult<Customer>>> getCustomerListBySearchKey(String searchKey);

    Mono<Void> cacheCustomerListBySearchKey(String searchKey, PaginatedResult<Customer> result);

    Mono<Void> invalidateAllCustomerListCaches();

    Mono<Void> invalidateCustomerDetailCache(String customerId);
}
