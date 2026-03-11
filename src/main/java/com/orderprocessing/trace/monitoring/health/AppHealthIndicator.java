package com.orderprocessing.trace.monitoring.health;

import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Monitoring Enhancement: Application Health Indicator
 *
 * Exposes custom JVM + application health details via /actuator/health.
 * - Heap usage warning above 85%
 * - Uptime reporting
 * - Ready flag (can be set programmatically during startup)
 */
@Component
public class AppHealthIndicator implements HealthIndicator {

    private volatile boolean ready = true;

    @Override
    public Health health() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memBean.getHeapMemoryUsage().getUsed();
        long heapMax  = memBean.getHeapMemoryUsage().getMax();
        double heapPct = heapMax > 0 ? (heapUsed * 100.0 / heapMax) : 0;

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        Health.Builder builder = ready ? Health.up() : Health.down();
        builder
            .withDetail("service", "trace-context-lib")
            .withDetail("heap_used_mb", heapUsed / 1_048_576)
            .withDetail("heap_max_mb",  heapMax  / 1_048_576)
            .withDetail("heap_pct",     String.format("%.1f%%", heapPct))
            .withDetail("uptime_sec",   uptimeMs / 1000);

        if (heapPct > 85) {
            builder.withDetail("heap_warning", "Heap usage above 85% — consider GC tuning");
        }

        return builder.build();
    }

    public void setReady(boolean ready) { this.ready = ready; }
}
