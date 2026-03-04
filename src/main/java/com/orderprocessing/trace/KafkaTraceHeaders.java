package com.orderprocessing.trace;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;

/**
 * Constants and utility methods for propagating {@link TraceContext}
 * through Kafka message headers.
 *
 * <p>Producer usage:
 * <pre>
 *   ProducerRecord&lt;String, String&gt; record = new ProducerRecord&lt;&gt;("topic", key, value);
 *   KafkaTraceHeaders.inject(TraceContextHolder.get(), record.headers());
 *   kafkaTemplate.send(record);
 * </pre>
 *
 * <p>Consumer usage:
 * <pre>
 *   TraceContext ctx = KafkaTraceHeaders.extract(consumerRecord.headers(), "downstream-service");
 *   TraceContextHolder.set(ctx);
 * </pre>
 */
public final class KafkaTraceHeaders {

    public static final String TRACE_ID       = "X-Trace-Id";
    public static final String SPAN_ID        = "X-Span-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String SERVICE_NAME   = "X-Service-Name";

    private KafkaTraceHeaders() {}

    /** Injects the current {@link TraceContext} into Kafka message headers. */
    public static void inject(TraceContext ctx, Headers headers) {
        if (ctx == null) return;
        setHeader(headers, TRACE_ID,       ctx.getTraceId());
        setHeader(headers, SPAN_ID,         ctx.getSpanId());
        setHeader(headers, SERVICE_NAME,    ctx.getServiceName());
        if (ctx.getCorrelationId() != null) {
            setHeader(headers, CORRELATION_ID, ctx.getCorrelationId());
        }
    }

    /**
     * Extracts a {@link TraceContext} from Kafka consumer record headers.
     * Creates a child span (new spanId) for the consuming service.
     *
     * @param headers         Kafka headers from the consumer record
     * @param consumerService name of the consuming service
     */
    public static TraceContext extract(Headers headers, String consumerService) {
        String traceId = getHeader(headers, TRACE_ID);
        String correlationId = getHeader(headers, CORRELATION_ID);

        if (traceId != null && !traceId.isBlank()) {
            TraceContext ctx = TraceIdGenerator.createChildContext(traceId, consumerService);
            if (correlationId != null) {
                return TraceContext.builder()
                        .traceId(ctx.getTraceId()).spanId(ctx.getSpanId())
                        .correlationId(correlationId).serviceName(consumerService)
                        .timestamp(ctx.getTimestamp()).build();
            }
            return ctx;
        }
        // No trace in message — generate new
        return TraceIdGenerator.createContext("KFK", consumerService, null);
    }

    private static void setHeader(Headers headers, String key, String value) {
        if (value != null) headers.add(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private static String getHeader(Headers headers, String key) {
        org.apache.kafka.common.header.Header h = headers.lastHeader(key);
        return h != null ? new String(h.value(), StandardCharsets.UTF_8) : null;
    }
}
