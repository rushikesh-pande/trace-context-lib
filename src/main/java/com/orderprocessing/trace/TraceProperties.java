package com.orderprocessing.trace;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized configuration for trace-context-lib.
 *
 * <p>Configure in {@code application.properties}:
 * <pre>
 *   trace.service-name=createorder
 *   trace.service-prefix=ORD
 *   trace.enabled=true
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "trace")
public class TraceProperties {

    /** Human-readable service name embedded in every TraceContext */
    private String serviceName = "unknown-service";

    /** Short prefix for generated traceIds (e.g. ORD, PAY, PROC) */
    private String servicePrefix = "SVC";

    /** Whether tracing is enabled (default true) */
    private boolean enabled = true;
}
