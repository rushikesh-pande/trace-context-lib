package com.orderprocessing.trace.db.health;

import org.springframework.boot.actuate.health.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Database Optimisation Enhancement: Redis Health Indicator
 *
 * Pings Redis to verify the caching layer is available.
 * If Redis is down, the service still operates (cache misses hit DB).
 */
@Component("redisHealth")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (var conn = connectionFactory.getConnection()) {
            String pong = new String(conn.ping());
            return Health.up()
                    .withDetail("service", "trace-context-lib")
                    .withDetail("redis", "reachable")
                    .withDetail("ping", pong)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("service", "trace-context-lib")
                    .withDetail("redis", "unreachable — cache misses will hit DB")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
