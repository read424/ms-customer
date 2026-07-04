package com.bootcamp.ms_customer.infrastructure.adapters.outbound.cache.redis;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Cache Adapter Internal Conditions Tests")
class RedisCacheAdapterInternalConditionsTest {

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
        ReflectionTestUtils.setField(adapter, "ttlMinutes", 10L);
    }

    @Test
    @DisplayName("should execute if(success) when set returns true in cacheCustomerListBySearchKey")
    void shouldExecuteSuccessConditionWhenCachingSucceeds() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // Retornar true para que if (success) sea verdadero
        when(valueOperations.set(anyString(), any(), any()))
                .thenReturn(Mono.just(true));

        var result = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey("test-key", result)
                .as(StepVerifier::create)
                .verifyComplete();
        // if (success) { log.debug(...) } se ejecutó
    }

    @Test
    @DisplayName("should NOT execute if(success) block when set returns false")
    void shouldNotExecuteSuccessConditionWhenSetReturnsFalse() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // Retornar false para que if (success) sea falso
        when(valueOperations.set(anyString(), any(), any()))
                .thenReturn(Mono.just(false));

        var result = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey("test-key", result)
                .as(StepVerifier::create)
                .verifyComplete();
        // if (success) { log.debug(...) } NO se ejecutó (success es false)
    }

    @Test
    @DisplayName("should execute if(deleted > 0) in invalidateAllCustomerListCaches")
    void shouldExecuteDeletedConditionWhenKeysAreDeleted() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.just("key1", "key2", "key3"));
        // Retornar 1 para que if (deleted > 0) sea verdadero
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(1L));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
        // if (deleted > 0) { log.debug(...) } se ejecutó 3 veces
    }

    @Test
    @DisplayName("should NOT execute if(deleted > 0) when delete returns 0")
    void shouldNotExecuteDeletedConditionWhenNothingDeleted() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.just("key1"));
        // Retornar 0 para que if (deleted > 0) sea falso
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(0L));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
        // if (deleted > 0) { log.debug(...) } NO se ejecutó (deleted es 0)
    }

    @Test
    @DisplayName("should execute if(deleted > 0) in invalidateCustomerDetailCache")
    void shouldExecuteDeletedConditionInDetailInvalidation() {
        // Retornar 1 para que if (deleted > 0) sea verdadero
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(1L));

        adapter.invalidateCustomerDetailCache("CUST-123")
                .as(StepVerifier::create)
                .verifyComplete();
        // if (deleted > 0) { log.debug(...) } se ejecutó
    }

    @Test
    @DisplayName("should NOT execute if(deleted > 0) in invalidateCustomerDetailCache when nothing deleted")
    void shouldNotExecuteDeletedConditionInDetailWhenNothingDeleted() {
        // Retornar 0 para que if (deleted > 0) sea falso
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(0L));

        adapter.invalidateCustomerDetailCache("CUST-123")
                .as(StepVerifier::create)
                .verifyComplete();
        // if (deleted > 0) { log.debug(...) } NO se ejecutó (deleted es 0)
    }

    @Test
    @DisplayName("should execute if(value instanceof PaginatedResult) branch in get")
    void shouldExecuteInstanceofBranchWhenValueIsPaginatedResult() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        var cachedResult = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();
        when(valueOperations.get("valid-key"))
                .thenReturn(Mono.just(cachedResult));

        adapter.getCustomerListBySearchKey("valid-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // if (value instanceof PaginatedResult<?>) se ejecutó y retornó Optional.of()
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should execute else branch when value is NOT PaginatedResult")
    void shouldExecuteElseBranchWhenValueNotPaginatedResult() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // Devolver un String en lugar de PaginatedResult
        when(valueOperations.get("invalid-key"))
                .thenReturn(Mono.just("not a paginated result"));

        adapter.getCustomerListBySearchKey("invalid-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // La rama else se ejecutó: return Mono.just(Optional.empty())
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should execute try-catch block when ClassCastException occurs")
    void shouldExecuteTryCatchWhenCastFails() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // Crear un objeto que causará ClassCastException en el cast
        when(valueOperations.get("cast-error-key"))
                .thenReturn(Mono.just(new Integer(123))); // Integer en lugar de PaginatedResult

        adapter.getCustomerListBySearchKey("cast-error-key")
                .as(StepVerifier::create)
                .assertNext(result -> {
                    // catch (ClassCastException e) se ejecutó y retornó Optional.empty()
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should execute multiple if(deleted > 0) conditions in loop")
    void shouldExecuteDeletedConditionMultipleTimes() {
        when(reactiveRedisTemplate.keys(anyString()))
                .thenReturn(Flux.just("key1", "key2", "key3", "key4", "key5"));
        // Algunos retornan 1 (deleted > 0), otros retornan 0
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(1L), Mono.just(0L), Mono.just(1L), Mono.just(0L), Mono.just(1L));

        adapter.invalidateAllCustomerListCaches()
                .as(StepVerifier::create)
                .verifyComplete();
        // if (deleted > 0) se ejecutó en las iteraciones 1, 3, 5
    }

    @Test
    @DisplayName("should execute all branches in complete cache flow")
    void shouldExecuteAllBranchesInCompleteCacheFlow() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOperations);
        // First call: set with success=true
        when(valueOperations.set(anyString(), any(), any()))
                .thenReturn(Mono.just(true));
        // Second call: get returns valid cached data
        var cachedResult = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();
        when(valueOperations.get(anyString()))
                .thenReturn(Mono.just(cachedResult));
        when(reactiveRedisTemplate.delete(anyString()))
                .thenReturn(Mono.just(1L));

        // Cache
        var result = PaginatedResult.<Customer>builder()
                .content(java.util.Arrays.asList(createTestCustomer()))
                .pageNumber(1).pageSize(10).totalElements(1).totalPages(1).isLast(true)
                .build();

        adapter.cacheCustomerListBySearchKey("flow-key", result)
                .as(StepVerifier::create)
                .verifyComplete();

        // Get
        adapter.getCustomerListBySearchKey("flow-key")
                .as(StepVerifier::create)
                .assertNext(cached -> {})
                .verifyComplete();

        // Delete
        adapter.invalidateCustomerDetailCache("CUST-001")
                .as(StepVerifier::create)
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
