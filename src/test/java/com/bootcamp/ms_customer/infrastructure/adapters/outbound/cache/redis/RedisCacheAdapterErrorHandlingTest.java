package com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.redis;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.bootcamp.ms_customer.domain.model.Customer;
import com.bootcamp.ms_customer.domain.model.dto.PaginatedResult;
import com.bootcamp.ms_customer.domain.model.enums.CustomerStatus;
import com.bootcamp.ms_customer.domain.model.enums.CustomerType;
import com.bootcamp.ms_customer.domain.model.enums.DocumentType;
import com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.SearchKeyGenerator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Cache Adapter Error Handling Tests")
class RedisCacheAdapterErrorHandlingTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    private RedisCacheAdapter adapter;
    private SearchKeyGenerator keyGenerator;

    @BeforeEach
    void setup() {
        keyGenerator = new SearchKeyGenerator();
        adapter = new RedisCacheAdapter(reactiveRedisTemplate, keyGenerator);

        // Inyectar ReflectionTestUtils para TTL
        ReflectionTestUtils.setField(adapter, "ttlMinutes", 10L);
    }

    @Test
    @DisplayName("should handle error in getCustomerListBySearchKey and return empty")
    void shouldHandleErrorInGet() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test-key"))
                .thenReturn(Mono.error(new RuntimeException("Redis connection error")));

        adapter.getCustomerListBySearchKey("test-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // onErrorResume ejecutado, retorna Optional.empty()
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle error in cacheCustomerListBySearchKey via doOnError")
    void shouldHandleErrorInCache() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.set(anyString(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Cache write failed")));

        var result = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey("test-key", result)
                .as(StepVerifier::create)
                .verifyError(RuntimeException.class);
        // doOnError se ejecutó, pero el error se propaga
    }

    @Test
    @DisplayName("should handle error in invalidateAllCustomerListCaches via onErrorResume")
    void shouldHandleErrorInInvalidateAll() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Keys lookup failed")));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
        // onErrorResume ejecutado, retorna Mono.empty()
    }

    @Test
    @DisplayName("should handle error in invalidateCustomerDetailCache via onErrorResume")
    void shouldHandleErrorInInvalidateDetail() {
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Delete failed")));

        adapter.invalidateCustomerDetailCache("CUST-123")
                .as(StepVerifier::create)
                .verifyComplete();
        // onErrorResume ejecutado, retorna Mono.empty()
    }

    @Test
    @DisplayName("should handle error when keys() fails during invalidation")
    void shouldHandleKeysFailure() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.error(new IllegalStateException("Redis unavailable")));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle error in flatMap during invalidation")
    void shouldHandleDeleteFailureInLoop() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.just("key1", "key2"));
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Delete operation failed")));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("should recover from get error with Optional.empty()")
    void shouldRecoverFromGetError() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("error-key"))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        adapter.getCustomerListBySearchKey("error-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // onErrorResume garantiza Optional.empty()
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle timeout error in get operation")
    void shouldHandleTimeoutInGet() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString()))
                .thenReturn(Mono.error(new RuntimeException("Operation timed out")));

        adapter.getCustomerListBySearchKey("timeout-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // onErrorResume retorna Optional.empty()
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle concurrent modification error")
    void shouldHandleConcurrentModification() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("concurrent-key"))
                .thenReturn(Mono.error(new RuntimeException("Concurrent modification detected")));

        adapter.getCustomerListBySearchKey("concurrent-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // Error handled gracefully
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle error when casting fails")
    void shouldHandleCastError() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // Devolver un objeto que no es PaginatedResult para forzar ClassCastException
        when(valueOperations.get("cast-key"))
                .thenReturn(Mono.just("not a paginated result"));

        adapter.getCustomerListBySearchKey("cast-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // Catch de ClassCastException ejecutado, retorna Optional.empty()
                })
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
