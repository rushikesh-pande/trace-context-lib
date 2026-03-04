package com.orderprocessing.trace;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

/**
 * Immutable value object that carries distributed tracing information
 * through the request lifecycle.
 *
 * <p>Propagated via:
 * <ul>
 *   <li>HTTP header  : {@code X-Trace-Id}
 *   <li>HTTP header  : {@code X-Correlation-Id}
 *   <li>HTTP header  : {@code X-Span-Id}
 *   <li>Kafka header : {@link KafkaTraceHeaders#TRACE_ID}
 * </ul>
 */
@Data
@Builder
public class TraceContext {

    /** Primary trace identifier — UUID, propagated end-to-end */
    private final String traceId;

    /** Span identifier — unique per service hop */
    private final String spanId;

    /** Optional correlation ID (e.g. customer session, batch ID) */
    private final String correlationId;

    /** Name of the originating service */
    private final String serviceName;

    /** UTC timestamp when the trace was created */
    private final Instant timestamp;

    /**
     * Returns a one-line log-friendly representation.
     * Format: {@code traceId=xxx spanId=yyy service=zzz}
     */
    public String toLogString() {
        return String.format("traceId=%s spanId=%s correlationId=%s service=%s",
                traceId, spanId, correlationId != null ? correlationId : "-", serviceName);
    }
}
