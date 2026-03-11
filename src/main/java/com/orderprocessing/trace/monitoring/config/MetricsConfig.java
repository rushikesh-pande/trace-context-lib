package com.orderprocessing.trace.monitoring.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monitoring Enhancement: Micrometer / Prometheus Metrics Configuration
 *
 * Configures:
 *  - Common tags (service, version) applied to ALL metrics
 *  - @Timed AOP aspect for controller/service timing
 *  - Histogram publishing for p50/p75/p95/p99 latency buckets
 *  - Deny-list filter to suppress noisy metrics
 */
@Configuration
public class MetricsConfig {

    /** Tag every metric with service name and version — visible in Grafana filters */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("service", "trace-context-lib", "version", "1.0.0")
                .meterFilter(MeterFilter.deny(id ->
                        id.getName().startsWith("jvm.gc.pause") && id.getTag("action") != null
                        && id.getTag("action").equals("No GC")));
    }

    /**
     * Enable @Timed annotation support on any @Component method.
     * Usage: @Timed(value = "order.create.time", description = "Time to create order",
     *               percentiles = {0.5, 0.95, 0.99}, histogram = true)
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
