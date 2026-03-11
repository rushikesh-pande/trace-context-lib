package com.orderprocessing.trace.db.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Optimisation Enhancement: Redis Cache Configuration
 *
 * Configures per-cache TTLs using Spring Cache + Redis.
 * Each cache has an appropriate TTL based on data volatility:
 *  - short-lived data (orders in-flight): 60s
 *  - medium (product/inventory lookups): 5min
 *  - reference data (static lists): 30min
 *
 * Usage in service:
 *   @Cacheable(value = "tracecontextlibCache", key = "#id")
 *   @CacheEvict(value = "tracecontextlibCache", key = "#id")
 *   @CachePut(value  = "tracecontextlibCache", key = "#result.id")
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Duration SHORT  = Duration.ofSeconds(60);
    private static final Duration MEDIUM = Duration.ofMinutes(5);
    private static final Duration LONG   = Duration.ofMinutes(30);

    /** Default serializer: JSON value, String key */
    private RedisCacheConfiguration defaultConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        Map<String, RedisCacheConfiguration> caches = new HashMap<>();

        // ── trace-context-lib specific caches ─────────────────────────────────────
        caches.put("tracecontextlibCache",       defaultConfig().entryTtl(MEDIUM));
        caches.put("tracecontextlibListCache",   defaultConfig().entryTtl(SHORT));
        caches.put("tracecontextlibCountCache",  defaultConfig().entryTtl(SHORT));
        caches.put("tracecontextlibRefCache",    defaultConfig().entryTtl(LONG));

        // ── Shared reference caches ──────────────────────────────────────────
        caches.put("productCache",        defaultConfig().entryTtl(MEDIUM));
        caches.put("userCache",           defaultConfig().entryTtl(MEDIUM));
        caches.put("configCache",         defaultConfig().entryTtl(LONG));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig().entryTtl(MEDIUM))
                .withInitialCacheConfigurations(caches)
                .transactionAware()
                .build();
    }
}
