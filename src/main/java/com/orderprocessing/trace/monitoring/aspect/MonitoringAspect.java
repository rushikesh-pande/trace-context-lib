package com.orderprocessing.trace.monitoring.aspect;

import io.micrometer.core.instrument.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Monitoring Enhancement: AOP Monitoring Aspect
 *
 * Automatically wraps all @RestController and @Service methods with:
 *  - Execution time measurement (timer)
 *  - Success / failure counting
 *  - Exception logging with correlation context
 *  - Prometheus metric emission
 *
 * No changes needed in business code — just add this aspect once.
 */
@Aspect
@Component
public class MonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(MonitoringAspect.class);

    private final MeterRegistry meterRegistry;

    public MonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Time all REST controller method calls and count successes/failures.
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object monitorControllerMethod(ProceedingJoinPoint pjp) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        String metricName = "tracecontextlib.controller.duration";

        Timer.Sample sample = Timer.start(meterRegistry);
        String status = "success";
        try {
            Object result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            status = "error";
            log.error("[MONITORING] Controller error in {}.{}: {}", className, methodName, ex.getMessage());
            Counter.builder("tracecontextlib.controller.errors")
                   .tag("service", "trace-context-lib")
                   .tag("class",  className)
                   .tag("method", methodName)
                   .register(meterRegistry).increment();
            throw ex;
        } finally {
            sample.stop(Timer.builder(metricName)
                    .tag("service", "trace-context-lib")
                    .tag("class",   className)
                    .tag("method",  methodName)
                    .tag("status",  status)
                    .publishPercentileHistogram()
                    .register(meterRegistry));
        }
    }

    /**
     * Time all @Service method calls.
     */
    @Around("within(@org.springframework.stereotype.Service *)")
    public Object monitorServiceMethod(ProceedingJoinPoint pjp) throws Throwable {
        String className  = pjp.getTarget().getClass().getSimpleName();
        String methodName = pjp.getSignature().getName();
        long   start      = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            log.warn("[MONITORING] Service exception in {}.{}: {}", className, methodName, ex.getMessage());
            throw ex;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > 500) {
                log.warn("[APM] SLOW SERVICE METHOD: {}.{} took {}ms", className, methodName, elapsed);
            }
            meterRegistry.timer("tracecontextlib.service.duration",
                    "service", "trace-context-lib",
                    "class",  className,
                    "method", methodName)
                    .record(elapsed, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }
}
