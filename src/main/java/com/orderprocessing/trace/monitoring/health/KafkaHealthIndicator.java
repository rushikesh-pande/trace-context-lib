package com.orderprocessing.trace.monitoring.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Monitoring Enhancement: Kafka Health Indicator
 *
 * Checks Kafka broker connectivity as part of /actuator/health endpoint.
 * Kubernetes readiness probes will fail if Kafka is DOWN.
 * Shows up as "kafka" component in health check response.
 */
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Override
    public Health health() {
        try (AdminClient client = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                       AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000",
                       AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "3000"))) {
            client.listTopics().names().get(3, TimeUnit.SECONDS);
            return Health.up()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("status", "Kafka broker reachable")
                    .build();
        } catch (Exception ex) {
            return Health.down()
                    .withDetail("bootstrap-servers", bootstrapServers)
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}
