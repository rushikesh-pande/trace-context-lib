package com.orderprocessing.trace.db.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Database Optimisation Enhancement: Database Metrics Service
 *
 * Tracks cache and query performance metrics for trace-context-lib.
 * Exposed to Prometheus via /actuator/prometheus.
 *
 * Metrics:
 *  - tracecontextlib_cache_hits_total       — Redis cache hits
 *  - tracecontextlib_cache_misses_total     — Redis cache misses (DB queries)
 *  - tracecontextlib_db_queries_total       — Total DB queries by type
 *  - tracecontextlib_db_slow_queries_total  — Queries above 500ms
 *  - tracecontextlib_connection_pool_active — HikariCP active connections
 */
@Service
public class DatabaseMetricsService {

    private final MeterRegistry meterRegistry;
    private final AtomicLong activeConnections = new AtomicLong(0);

    public DatabaseMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        Gauge.builder("tracecontextlib.connection.pool.active", activeConnections, AtomicLong::get)
             .description("Active HikariCP connections for trace-context-lib")
             .tag("service", "trace-context-lib")
             .register(meterRegistry);
    }

    public void recordCacheHit(String cacheName) {
        Counter.builder("tracecontextlib.cache.hits.total")
               .tag("service", "trace-context-lib").tag("cache", cacheName)
               .description("Redis cache hits for trace-context-lib")
               .register(meterRegistry).increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("tracecontextlib.cache.misses.total")
               .tag("service", "trace-context-lib").tag("cache", cacheName)
               .description("Redis cache misses for trace-context-lib (DB fallback)")
               .register(meterRegistry).increment();
    }

    public void recordDbQuery(String queryType) {
        Counter.builder("tracecontextlib.db.queries.total")
               .tag("service", "trace-context-lib").tag("type", queryType)
               .description("DB queries for trace-context-lib")
               .register(meterRegistry).increment();
    }

    public void recordSlowQuery(String queryType, long ms) {
        Counter.builder("tracecontextlib.db.slow.queries.total")
               .tag("service", "trace-context-lib").tag("type", queryType)
               .description("DB queries exceeding 500ms for trace-context-lib")
               .register(meterRegistry).increment();
        meterRegistry.summary("tracecontextlib.db.query.duration",
                "service", "trace-context-lib", "type", queryType).record(ms);
    }

    public void setActiveConnections(long count) {
        activeConnections.set(count);
    }
}
