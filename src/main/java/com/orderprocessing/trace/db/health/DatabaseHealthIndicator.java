package com.orderprocessing.trace.db.health;

import org.springframework.boot.actuate.health.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Database Optimisation Enhancement: Database Health Indicator
 *
 * Checks primary DB connectivity via a lightweight validation query.
 * Kubernetes readiness probe will fail if DB is unhealthy.
 * Exposed at /actuator/health under "database" component.
 */
@Component("databaseHealth")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            Long result = jdbcTemplate.queryForObject("SELECT 1", Long.class);
            return Health.up()
                    .withDetail("service", "trace-context-lib")
                    .withDetail("database", "reachable")
                    .withDetail("validation_query", "SELECT 1 = " + result)
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("service", "trace-context-lib")
                    .withDetail("database", "unreachable")
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
