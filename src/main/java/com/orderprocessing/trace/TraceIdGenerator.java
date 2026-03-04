package com.orderprocessing.trace;

import java.time.Instant;
import java.util.UUID;

/**
 * Utility class for generating trace and span identifiers.
 *
 * <p>Format:
 * <ul>
 *   <li>TraceId: {@code <SERVICE_PREFIX>-<UUID_WITHOUT_DASHES>}
 *       e.g. {@code ORD-3f4a1b2c8d9e0f1a2b3c4d5e6f7a8b9c}
 *   <li>SpanId : {@code SP-<first 16 chars of UUID>}
 * </ul>
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {}

    /**
     * Generates a new TraceId with an optional service prefix.
     *
     * @param servicePrefix short prefix (e.g. "ORD", "PAY", "PROC") — may be null
     * @return a unique trace ID string
     */
    public static String generate(String servicePrefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (servicePrefix != null && !servicePrefix.isBlank()) {
            return servicePrefix.toUpperCase() + "-" + uuid;
        }
        return "TRC-" + uuid;
    }

    /** Generates a new TraceId without prefix */
    public static String generate() {
        return generate(null);
    }

    /**
     * Generates a short SpanId (16 hex chars prefixed with "SP-").
     * Each service hop should create a new spanId while keeping the same traceId.
     */
    public static String generateSpanId() {
        return "SP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Creates a fully populated {@link TraceContext} for a service.
     *
     * @param servicePrefix short name used in traceId prefix
     * @param serviceName   full service name stored in context
     * @param correlationId optional business correlation id (may be null)
     */
    public static TraceContext createContext(String servicePrefix, String serviceName, String correlationId) {
        return TraceContext.builder()
                .traceId(generate(servicePrefix))
                .spanId(generateSpanId())
                .correlationId(correlationId)
                .serviceName(serviceName)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Creates a child context that inherits the parent traceId but gets a new spanId.
     * Use this when passing the trace to a downstream service.
     */
    public static TraceContext createChildContext(String parentTraceId, String serviceName) {
        return TraceContext.builder()
                .traceId(parentTraceId)
                .spanId(generateSpanId())
                .correlationId(null)
                .serviceName(serviceName)
                .timestamp(Instant.now())
                .build();
    }
}
