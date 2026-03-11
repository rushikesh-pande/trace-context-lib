package com.orderprocessing.trace.monitoring.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

/**
 * Monitoring Enhancement: Correlation ID Filter
 * Extracts X-Correlation-ID / X-Request-ID from incoming HTTP headers.
 * If not present, generates a new UUID. Adds to MDC so every log line
 * automatically carries the correlation context.
 *
 * Compatible with ELK Stack logback-spring.xml JSON encoder.
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER      = "X-Request-ID";
    public static final String MDC_CORRELATION_KEY    = "correlationId";
    public static final String MDC_REQUEST_KEY        = "requestId";
    public static final String MDC_SERVICE_KEY        = "service";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  httpReq = (HttpServletRequest)  req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        String correlationId = httpReq.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String requestId = httpReq.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_CORRELATION_KEY, correlationId);
        MDC.put(MDC_REQUEST_KEY,     requestId);
        MDC.put(MDC_SERVICE_KEY,     "trace-context-lib");

        // Echo correlation-id back to caller
        httpRes.setHeader(CORRELATION_ID_HEADER, correlationId);
        httpRes.setHeader(REQUEST_ID_HEADER,      requestId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_CORRELATION_KEY);
            MDC.remove(MDC_REQUEST_KEY);
            MDC.remove(MDC_SERVICE_KEY);
        }
    }
}
