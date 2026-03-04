package com.orderprocessing.trace;

import org.slf4j.MDC;

/**
 * ThreadLocal-based store for the current request's {@link TraceContext}.
 *
 * <p>The holder also keeps SLF4J MDC in sync so that every log statement
 * automatically includes traceId and spanId without manual intervention.
 *
 * <p>MDC keys: {@code traceId}, {@code spanId}, {@code correlationId}, {@code serviceName}
 *
 * <p>Usage:
 * <pre>
 *   // In a filter/interceptor:
 *   TraceContextHolder.set(ctx);
 *   try {
 *       chain.doFilter(request, response);
 *   } finally {
 *       TraceContextHolder.clear();
 *   }
 * </pre>
 */
public final class TraceContextHolder {

    public static final String MDC_TRACE_ID       = "traceId";
    public static final String MDC_SPAN_ID        = "spanId";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_SERVICE_NAME   = "serviceName";

    private static final ThreadLocal<TraceContext> HOLDER = new ThreadLocal<>();

    private TraceContextHolder() {}

    /** Store a {@link TraceContext} in the current thread and synchronize MDC. */
    public static void set(TraceContext ctx) {
        HOLDER.set(ctx);
        if (ctx != null) {
            MDC.put(MDC_TRACE_ID,       ctx.getTraceId());
            MDC.put(MDC_SPAN_ID,        ctx.getSpanId());
            MDC.put(MDC_CORRELATION_ID, ctx.getCorrelationId() != null ? ctx.getCorrelationId() : "-");
            MDC.put(MDC_SERVICE_NAME,   ctx.getServiceName()   != null ? ctx.getServiceName()   : "-");
        }
    }

    /** Returns the current thread's {@link TraceContext}, or {@code null} if none. */
    public static TraceContext get() {
        return HOLDER.get();
    }

    /** Returns the current traceId string, or {@code "UNKNOWN"} if no context. */
    public static String getTraceId() {
        TraceContext ctx = HOLDER.get();
        return ctx != null ? ctx.getTraceId() : "UNKNOWN";
    }

    /** Returns the current spanId string, or {@code "UNKNOWN"} if no context. */
    public static String getSpanId() {
        TraceContext ctx = HOLDER.get();
        return ctx != null ? ctx.getSpanId() : "UNKNOWN";
    }

    /** Clears the ThreadLocal and removes MDC keys — MUST be called in finally block. */
    public static void clear() {
        HOLDER.remove();
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_SERVICE_NAME);
    }
}
