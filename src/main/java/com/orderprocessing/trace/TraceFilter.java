package com.orderprocessing.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Servlet filter that intercepts every inbound HTTP request and:
 * <ol>
 *   <li>Reads {@code X-Trace-Id} header if present (propagated from upstream service)
 *   <li>Generates a new traceId if none is present
 *   <li>Stores the {@link TraceContext} in {@link TraceContextHolder}
 *   <li>Injects traceId back into the HTTP response as {@code X-Trace-Id}
 *   <li>Clears the ThreadLocal after the request completes
 * </ol>
 *
 * <p>Registered automatically by {@link TraceAutoConfig}.
 */
@Slf4j
public class TraceFilter extends OncePerRequestFilter {

    public static final String HEADER_TRACE_ID       = "X-Trace-Id";
    public static final String HEADER_SPAN_ID        = "X-Span-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";

    private final String serviceName;
    private final String servicePrefix;

    public TraceFilter(String serviceName, String servicePrefix) {
        this.serviceName   = serviceName;
        this.servicePrefix = servicePrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String incomingTraceId = request.getHeader(HEADER_TRACE_ID);
        String correlationId   = request.getHeader(HEADER_CORRELATION_ID);

        TraceContext ctx;
        if (incomingTraceId != null && !incomingTraceId.isBlank()) {
            // Propagated trace from upstream — create child span
            ctx = TraceIdGenerator.createChildContext(incomingTraceId, serviceName);
            if (correlationId != null) {
                ctx = TraceContext.builder()
                        .traceId(ctx.getTraceId())
                        .spanId(ctx.getSpanId())
                        .correlationId(correlationId)
                        .serviceName(serviceName)
                        .timestamp(ctx.getTimestamp())
                        .build();
            }
            log.debug("[TRACE] Received propagated traceId={} from upstream — new spanId={}",
                    incomingTraceId, ctx.getSpanId());
        } else {
            // No incoming trace — this is the origin service
            ctx = TraceIdGenerator.createContext(servicePrefix, serviceName, correlationId);
            log.debug("[TRACE] Generated new traceId={} spanId={}", ctx.getTraceId(), ctx.getSpanId());
        }

        TraceContextHolder.set(ctx);

        // Propagate to response headers
        response.setHeader(HEADER_TRACE_ID,       ctx.getTraceId());
        response.setHeader(HEADER_SPAN_ID,         ctx.getSpanId());
        if (ctx.getCorrelationId() != null) {
            response.setHeader(HEADER_CORRELATION_ID, ctx.getCorrelationId());
        }

        try {
            log.info("[TRACE] {} {} | {}", request.getMethod(), request.getRequestURI(), ctx.toLogString());
            filterChain.doFilter(request, response);
        } finally {
            TraceContextHolder.clear();
        }
    }
}
