package com.bootcamp.ms_customer.infrastructure.test.cache.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bootcamp.ms_customer.infrastructure.test.cache.config.CacheTestConfiguration;

@Testcontainers
public abstract class AbstractCacheAdapterIntegrationTest {

    protected static final AnnotationConfigApplicationContext applicationContext;
    protected ReactiveRedisTemplate<String, Object> redisTemplate;

    static {
        applicationContext = new AnnotationConfigApplicationContext(CacheTestConfiguration.class);
    }

    @BeforeEach
    void setupDependencies() {
        redisTemplate = applicationContext.getBean(ReactiveRedisTemplate.class);
    }

    @BeforeEach
    void cleanupRedis() {
        redisTemplate.keys("*")
                .flatMap(key -> redisTemplate.delete(key))
                .collectList()
                .block();
    }

    protected <T> T getBean(Class<T> beanType) {
        return applicationContext.getBean(beanType);
    }
}
