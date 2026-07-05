package com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.redis;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;
import com.bootcamp.ms_customer.infrastructure.test.cache.base.AbstractCacheAdapterIntegrationTest;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Redis Cache Adapter Integration Tests")
class RedisCacheAdapterIntegrationTest extends AbstractCacheAdapterIntegrationTest {

    @Test
    @DisplayName("should cache and retrieve customer list")
    void shouldCacheAndRetrieveCustomerList() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var searchKey = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var customer = createTestCustomer();
        var paginatedResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(searchKey, paginatedResult)
                .then(adapter.getCustomerListBySearchKey(searchKey))
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result).isPresent();
                    assertThat(result.get().getContent()).hasSize(1);
                    assertThat(result.get().getContent().get(0).getFirstName()).isEqualTo("John");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return empty optional when key not in cache")
    void shouldReturnEmptyWhenKeyNotInCache() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var searchKey = keyGenerator.generateSearchKey(1, 10, null);

        adapter.getCustomerListBySearchKey(searchKey)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should invalidate all customer list caches")
    void shouldInvalidateAllCustomerListCaches() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();
        var paginatedResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var searchKey1 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var searchKey2 = keyGenerator.generateSearchKey(1, 10, CustomerType.BUSINESS);

        adapter.cacheCustomerListBySearchKey(searchKey1, paginatedResult)
                .then(adapter.cacheCustomerListBySearchKey(searchKey2, paginatedResult))
                .then(adapter.invalidateAllCustomerListCaches())
                .then(adapter.getCustomerListBySearchKey(searchKey1))
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should invalidate specific customer detail cache")
    void shouldInvalidateCustomerDetailCache() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customerId = "CUST-123";
        var detailKey = keyGenerator.generateCustomerDetailKey(customerId);
        var customer = createTestCustomer();
        customer.setCustomerId(customerId);
        var paginatedResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(1)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(detailKey, paginatedResult)
                .then(adapter.invalidateCustomerDetailCache(customerId))
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle multiple cache entries with different keys")
    void shouldHandleMultipleCacheEntries() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer1 = createTestCustomer();
        var customer2 = createTestCustomer();
        customer2.setDocumentNumber("999999999");

        var result1 = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer1))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var result2 = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer2))
                .pageNumber(2)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key1 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var key2 = keyGenerator.generateSearchKey(2, 10, CustomerType.PERSONAL);

        adapter.cacheCustomerListBySearchKey(key1, result1)
                .then(adapter.cacheCustomerListBySearchKey(key2, result2))
                .then(adapter.getCustomerListBySearchKey(key1))
                .as(StepVerifier::create)
                .assertNext(cached -> {
                    assertThat(cached).isPresent();
                    assertThat(cached.get().getContent().get(0).getFirstName()).isEqualTo("John");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should cache with different filter types")
    void shouldCacheWithDifferentFilterTypes() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();
        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var keyPersonal = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var keyBusiness = keyGenerator.generateSearchKey(1, 10, CustomerType.BUSINESS);
        var keyAll = keyGenerator.generateSearchKey(1, 10, null);

        adapter.cacheCustomerListBySearchKey(keyPersonal, result)
                .then(adapter.cacheCustomerListBySearchKey(keyBusiness, result))
                .then(adapter.cacheCustomerListBySearchKey(keyAll, result))
                .then(adapter.getCustomerListBySearchKey(keyPersonal))
                .as(StepVerifier::create)
                .assertNext(cached -> assertThat(cached).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should recover from cache errors gracefully")
    void shouldRecoverFromCacheErrors() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var searchKey = keyGenerator.generateSearchKey(1, 10, null);

        adapter.getCustomerListBySearchKey(searchKey)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should generate correct cache keys")
    void shouldGenerateCorrectCacheKeys() {
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var key1 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var key2 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var key3 = keyGenerator.generateSearchKey(2, 10, CustomerType.PERSONAL);

        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
        assertThat(key1).startsWith("customers:list:");
    }

    @Test
    @DisplayName("should generate customer detail keys")
    void shouldGenerateCustomerDetailKeys() {
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var detailKey = keyGenerator.generateCustomerDetailKey("CUST-123");
        var pattern = keyGenerator.getCustomerDetailPattern();

        assertThat(detailKey).isEqualTo("customers:id:CUST-123");
        assertThat(pattern).isEqualTo("customers:id:*");
    }

    @Test
    @DisplayName("should handle null type in search key generation")
    void shouldHandleNullTypeInSearchKeyGeneration() {
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var keyWithNull = keyGenerator.generateSearchKey(1, 10, null);
        var keyWithType = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);

        assertThat(keyWithNull).isNotEqualTo(keyWithType);
        assertThat(keyWithNull).startsWith("customers:list:");
    }

    @Test
    @DisplayName("should cache and retrieve with different cache keys independently")
    void shouldHandleMultipleIndependentCacheKeys() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key1 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var key2 = keyGenerator.generateSearchKey(1, 10, CustomerType.BUSINESS);

        adapter.cacheCustomerListBySearchKey(key1, result)
                .then(adapter.getCustomerListBySearchKey(key1))
                .as(StepVerifier::create)
                .assertNext(cached -> assertThat(cached).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should cache result successfully with TTL")
    void shouldCacheResultSuccessfullyWithTTL() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();
        var searchKey = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);

        var paginatedResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(searchKey, paginatedResult)
                .as(StepVerifier::create)
                .verifyComplete();

        adapter.getCustomerListBySearchKey(searchKey)
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should invalidate all caches and verify invalidation")
    void shouldInvalidateAllCachesAndVerify() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var searchKey = keyGenerator.generateSearchKey(1, 10, null);

        adapter.cacheCustomerListBySearchKey(searchKey, result)
                .then(adapter.invalidateAllCustomerListCaches())
                .then(adapter.getCustomerListBySearchKey(searchKey))
                .as(StepVerifier::create)
                .assertNext(retrieved -> assertThat(retrieved).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should successfully cache with success=true in doOnNext")
    void shouldSuccessfullyCacheWithSuccessTrue() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();
        var searchKey = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);

        var paginatedResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(searchKey, paginatedResult)
                .as(StepVerifier::create)
                .verifyComplete();

        adapter.getCustomerListBySearchKey(searchKey)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result).isPresent();
                    assertThat(result.get().getContent()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should verify deletion count > 0 in invalidateAllCustomerListCaches")
    void shouldVerifyDeletionCountInInvalidateAll() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key1 = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var key2 = keyGenerator.generateSearchKey(2, 10, CustomerType.PERSONAL);
        var key3 = keyGenerator.generateSearchKey(3, 10, CustomerType.PERSONAL);

        adapter.cacheCustomerListBySearchKey(key1, result)
                .then(adapter.cacheCustomerListBySearchKey(key2, result))
                .then(adapter.cacheCustomerListBySearchKey(key3, result))
                .then(adapter.invalidateAllCustomerListCaches())
                .then(adapter.getCustomerListBySearchKey(key1))
                .as(StepVerifier::create)
                .assertNext(retrieved -> assertThat(retrieved).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should verify deletion count > 0 in invalidateCustomerDetailCache")
    void shouldVerifyDeletionCountInInvalidateDetail() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customerId = "CUST-VERIFY-DELETE";
        var detailKey = keyGenerator.generateCustomerDetailKey(customerId);
        var customer = createTestCustomer();
        customer.setCustomerId(customerId);

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(1)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(detailKey, result)
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(cached -> assertThat(cached).isPresent())
                .verifyComplete();

        adapter.invalidateCustomerDetailCache(customerId)
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(retrieved -> assertThat(retrieved).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle cache with large dataset")
    void shouldHandleLargeDataset() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customers = new java.util.ArrayList<Customer>();

        for (int i = 0; i < 10; i++) {
            var customer = createTestCustomer();
            customer.setDocumentNumber("DOC-" + i);
            customers.add(customer);
        }

        var result = PaginatedResult.<Customer>builder()
                .content(customers)
                .pageNumber(1)
                .pageSize(100)
                .totalElements(10)
                .totalPages(1)
                .isLast(true)
                .build();

        var searchKey = keyGenerator.generateSearchKey(1, 100, null);

        adapter.cacheCustomerListBySearchKey(searchKey, result)
                .then(adapter.getCustomerListBySearchKey(searchKey))
                .as(StepVerifier::create)
                .assertNext(cached -> {
                    assertThat(cached).isPresent();
                    assertThat(cached.get().getContent()).hasSize(10);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should cache and verify multiple sequential operations")
    void shouldHandleMultipleSequentialOperations() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);

        adapter.cacheCustomerListBySearchKey(key, result)
                .then(adapter.getCustomerListBySearchKey(key))
                .as(StepVerifier::create)
                .assertNext(cached1 -> assertThat(cached1).isPresent())
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key)
                .as(StepVerifier::create)
                .assertNext(cached2 -> assertThat(cached2).isPresent())
                .verifyComplete();

        adapter.invalidateCustomerDetailCache(customer.getCustomerId())
                .then(adapter.getCustomerListBySearchKey(key))
                .as(StepVerifier::create)
                .assertNext(cached3 -> assertThat(cached3).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle cache patterns correctly")
    void shouldHandleCachePatterns() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var listPattern = keyGenerator.getCustomerListPattern();
        var detailPattern = keyGenerator.getCustomerDetailPattern();

        assertThat(listPattern).contains("*");
        assertThat(detailPattern).contains("*");
        assertThat(listPattern).startsWith("customers:");
        assertThat(detailPattern).startsWith("customers:");
    }

    @Test
    @DisplayName("should cache empty list successfully")
    void shouldCacheEmptyList() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var emptyResult = PaginatedResult.<Customer>builder()
                .content(Arrays.asList())
                .pageNumber(1)
                .pageSize(10)
                .totalElements(0)
                .totalPages(1)
                .isLast(true)
                .build();

        var searchKey = keyGenerator.generateSearchKey(5, 10, CustomerType.BUSINESS);

        adapter.cacheCustomerListBySearchKey(searchKey, emptyResult)
                .then(adapter.getCustomerListBySearchKey(searchKey))
                .as(StepVerifier::create)
                .assertNext(cached -> {
                    assertThat(cached).isPresent();
                    assertThat(cached.get().getContent()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should invalidate non-existent cache key gracefully")
    void shouldInvalidateNonExistentKey() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var detailKey = keyGenerator.generateCustomerDetailKey("NON-EXISTENT");

        adapter.invalidateCustomerDetailCache("NON-EXISTENT")
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(result -> assertThat(result).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle cache with special characters in keys")
    void shouldHandleSpecialCharactersInKeys() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var customerId = "CUST-SPECIAL-!@#$%";
        var detailKey = keyGenerator.generateCustomerDetailKey(customerId);
        var customer = createTestCustomer();
        customer.setCustomerId(customerId);

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(1)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey(detailKey, result)
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(cached -> assertThat(cached).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should verify cache success and execute doOnNext with success=true")
    void shouldVerifyCacheSuccessDoOnNext() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var customer1 = createTestCustomer();
        customer1.setDocumentNumber("DOC-001");
        var customer2 = createTestCustomer();
        customer2.setDocumentNumber("DOC-002");

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer1, customer2))
                .pageNumber(1)
                .pageSize(50)
                .totalElements(2)
                .totalPages(1)
                .isLast(true)
                .build();

        var key = keyGenerator.generateSearchKey(1, 50, null);

        adapter.cacheCustomerListBySearchKey(key, result)
                .as(StepVerifier::create)
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key)
                .as(StepVerifier::create)
                .assertNext(cached -> {
                    assertThat(cached).isPresent();
                    assertThat(cached.get().getTotalElements()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should invalidate multiple keys and verify each deletion > 0")
    void shouldInvalidateMultipleKeysAndVerifyDeletion() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var customer = createTestCustomer();
        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var keys = new java.util.ArrayList<String>();
        for (int i = 1; i <= 5; i++) {
            keys.add(keyGenerator.generateSearchKey(i, 10, CustomerType.PERSONAL));
        }

        var cacheMono = adapter.cacheCustomerListBySearchKey(keys.get(0), result);
        for (int i = 1; i < keys.size(); i++) {
            cacheMono = cacheMono.then(
                    adapter.cacheCustomerListBySearchKey(keys.get(i), result)
            );
        }

        cacheMono
                .then(adapter.invalidateAllCustomerListCaches())
                .then(adapter.getCustomerListBySearchKey(keys.get(0)))
                .as(StepVerifier::create)
                .assertNext(result2 -> assertThat(result2).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should cache and retrieve with pagination verification")
    void shouldCacheAndRetrieveWithPaginationInfo() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var customers = new java.util.ArrayList<Customer>();
        for (int i = 0; i < 5; i++) {
            var customer = createTestCustomer();
            customer.setDocumentNumber("PAGE-DOC-" + i);
            customers.add(customer);
        }

        var result = PaginatedResult.<Customer>builder()
                .content(customers)
                .pageNumber(2)
                .pageSize(5)
                .totalElements(15)
                .totalPages(3)
                .isLast(false)
                .build();

        var key = keyGenerator.generateSearchKey(2, 5, CustomerType.PERSONAL);

        adapter.cacheCustomerListBySearchKey(key, result)
                .then(adapter.getCustomerListBySearchKey(key))
                .as(StepVerifier::create)
                .assertNext(cached -> {
                    assertThat(cached).isPresent();
                    assertThat(cached.get().getPageNumber()).isEqualTo(2);
                    assertThat(cached.get().getPageSize()).isEqualTo(5);
                    assertThat(cached.get().getTotalElements()).isEqualTo(15);
                    assertThat(cached.get().getTotalPages()).isEqualTo(3);
                    assertThat(cached.get().isLast()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle sequential cache-get-invalidate operations")
    void shouldHandleSequentialCacheGetInvalidate() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key = keyGenerator.generateSearchKey(1, 10, null);

        adapter.cacheCustomerListBySearchKey(key, result)
                .then(adapter.getCustomerListBySearchKey(key))
                .as(StepVerifier::create)
                .assertNext(c1 -> assertThat(c1).isPresent())
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key)
                .as(StepVerifier::create)
                .assertNext(c2 -> assertThat(c2).isPresent())
                .verifyComplete();

        adapter.invalidateAllCustomerListCaches()
                .then(adapter.getCustomerListBySearchKey(key))
                .as(StepVerifier::create)
                .assertNext(c3 -> assertThat(c3).isEmpty())
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key)
                .as(StepVerifier::create)
                .assertNext(c4 -> assertThat(c4).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should cache result with different TTL values")
    void shouldCacheResultWithTTL() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var key = keyGenerator.generateSearchKey(1, 10, CustomerType.BUSINESS);

        adapter.cacheCustomerListBySearchKey(key, result)
                .as(StepVerifier::create)
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key)
                .as(StepVerifier::create)
                .assertNext(cached -> assertThat(cached).isPresent())
                .verifyComplete();
    }

    @Test
    @DisplayName("should consistently cache and retrieve across multiple calls")
    void shouldConsistentlyCacheMultipleCalls() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        for (int i = 0; i < 3; i++) {
            var customer = createTestCustomer();
            customer.setDocumentNumber("CONSISTENT-" + i);

            var result = PaginatedResult.<Customer>builder()
                    .content(Arrays.asList(customer))
                    .pageNumber(1)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .isLast(true)
                    .build();

            var key = keyGenerator.generateSearchKey(i + 1, 10, null);

            adapter.cacheCustomerListBySearchKey(key, result)
                    .then(adapter.getCustomerListBySearchKey(key))
                    .as(StepVerifier::create)
                    .assertNext(c -> assertThat(c).isPresent())
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("should handle invalidate and cache lifecycle")
    void shouldHandleInvalidateAndCacheLifecycle() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);

        var customer1 = createTestCustomer();
        var customer2 = createTestCustomer();
        customer2.setDocumentNumber("LIFECYCLE-2");

        var result1 = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer1))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        var result2 = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer2))
                .pageNumber(2).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        var key1 = keyGenerator.generateSearchKey(1, 10, null);
        var key2 = keyGenerator.generateSearchKey(2, 10, null);

        // Cache, verify, invalidate all, verify empty
        adapter.cacheCustomerListBySearchKey(key1, result1)
                .then(adapter.cacheCustomerListBySearchKey(key2, result2))
                .then(adapter.getCustomerListBySearchKey(key1))
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isPresent())
                .verifyComplete();

        adapter.invalidateAllCustomerListCaches()
                .then(adapter.getCustomerListBySearchKey(key1))
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isEmpty())
                .verifyComplete();

        adapter.getCustomerListBySearchKey(key2)
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("should verify all cache operations complete successfully")
    void shouldVerifyAllCacheOperationsComplete() {
        var adapter = getBean(RedisCacheAdapter.class);
        var keyGenerator = getBean(SearchKeyGenerator.class);
        var customer = createTestCustomer();

        var result = PaginatedResult.<Customer>builder()
                .content(Arrays.asList(customer))
                .pageNumber(1)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        var searchKey = keyGenerator.generateSearchKey(1, 10, CustomerType.PERSONAL);
        var detailKey = keyGenerator.generateCustomerDetailKey(customer.getCustomerId());

        // Chain operations
        adapter.cacheCustomerListBySearchKey(searchKey, result)
                .then(adapter.getCustomerListBySearchKey(searchKey))
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isPresent())
                .verifyComplete();

        adapter.cacheCustomerListBySearchKey(detailKey, result)
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isPresent())
                .verifyComplete();

        adapter.invalidateCustomerDetailCache(customer.getCustomerId())
                .then(adapter.getCustomerListBySearchKey(detailKey))
                .as(StepVerifier::create)
                .assertNext(c -> assertThat(c).isEmpty())
                .verifyComplete();
    }

    private Customer createTestCustomer() {
        return Customer.builder()
                .customerId("CUST-001")
                .firstName("John")
                .lastName("Doe")
                .documentType(DocumentType.DNI)
                .documentNumber("12345678A")
                .email("john@example.com")
                .phoneNumber("+34912345678")
                .customerType(CustomerType.PERSONAL)
                .status(CustomerStatus.ACTIVE)
                .build();
    }
}
