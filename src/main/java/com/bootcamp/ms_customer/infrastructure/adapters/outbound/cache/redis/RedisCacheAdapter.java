package com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.redis;

import com.bootcamp.ms_customer.application.ports.output.CachePort;
import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements CachePort {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final SearchKeyGenerator searchKeyGenerator;

    @Value("${cache.redis.ttl-minutes:10}")
    private long ttlMinutes;

    @Override
    public Mono<Optional<PaginatedResult<Customer>>> getCustomerListBySearchKey(String searchKey) {
        return reactiveRedisTemplate.opsForValue()
                .get(searchKey)
                .<Optional<PaginatedResult<Customer>>>flatMap(value -> {
                    log.debug("Cache hit for key: {}", searchKey);
                    if (value instanceof PaginatedResult<?>) {
                        try {
                            @SuppressWarnings("unchecked")
                            PaginatedResult<Customer> result = (PaginatedResult<Customer>) value;
                            return Mono.just(Optional.of(result));
                        } catch (ClassCastException e) {
                            log.warn("Error casting cached value for key: {}", searchKey, e);
                            return Mono.just(Optional.empty());
                        }
                    }
                    return Mono.just(Optional.empty());
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache miss for key: {}", searchKey);
                    return Mono.just(Optional.empty());
                }))
                .onErrorResume(error -> {
                    log.warn("Error retrieving from cache for key: {}", searchKey, error);
                    return Mono.just(Optional.empty());
                });
    }

    @Override
    public Mono<Void> cacheCustomerListBySearchKey(String searchKey, PaginatedResult<Customer> result) {
        return reactiveRedisTemplate.opsForValue()
                .set(searchKey, result, Duration.ofMinutes(ttlMinutes))
                .doOnNext(success -> {
                    if (success) {
                        log.debug("Successfully cached customer list with key: {} for {} minutes",
                                searchKey, ttlMinutes);
                    }
                })
                .doOnError(error -> log.warn("Error caching customer list with key: {}", searchKey, error))
                .then();
    }

    @Override
    public Mono<Void> invalidateAllCustomerListCaches() {
        String pattern = searchKeyGenerator.getCustomerListPattern();
        return reactiveRedisTemplate.keys(pattern)
                .doOnNext(key -> log.debug("Found cache key for invalidation: {}", key))
                .flatMap(key -> reactiveRedisTemplate.delete(key)
                        .doOnNext(deleted -> {
                            if (deleted > 0) {
                                log.debug("Invalidated cache key: {}", key);
                            }
                        }))
                .then()
                .doOnSuccess(v -> log.info("All customer list caches invalidated with pattern: {}", pattern))
                .onErrorResume(error -> {
                    log.warn("Error invalidating all customer list caches", error);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> invalidateCustomerDetailCache(String customerId) {
        String detailKey = searchKeyGenerator.generateCustomerDetailKey(customerId);
        return reactiveRedisTemplate.delete(detailKey)
                .doOnNext(deleted -> {
                    if (deleted > 0) {
                        log.debug("Invalidated cache key for customer: {}", customerId);
                    }
                })
                .then()
                .onErrorResume(error -> {
                    log.warn("Error invalidating cache for customer: {}", customerId, error);
                    return Mono.empty();
                });
    }
}
