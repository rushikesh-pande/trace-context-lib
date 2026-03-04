package com.orderprocessing.trace;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TraceIdGeneratorTest {

    @Test
    void generateWithPrefix_shouldIncludePrefix() {
        String traceId = TraceIdGenerator.generate("ORD");
        assertTrue(traceId.startsWith("ORD-"), "traceId should start with prefix");
        assertTrue(traceId.length() > 10, "traceId should be long enough");
    }

    @Test
    void generate_withoutPrefix_shouldUseTRC() {
        String traceId = TraceIdGenerator.generate();
        assertTrue(traceId.startsWith("TRC-"));
    }

    @Test
    void generateSpanId_shouldStartWithSP() {
        String spanId = TraceIdGenerator.generateSpanId();
        assertTrue(spanId.startsWith("SP-"));
        assertEquals(19, spanId.length()); // SP- + 16 chars
    }

    @Test
    void createContext_shouldPopulateAllFields() {
        TraceContext ctx = TraceIdGenerator.createContext("ORD", "createorder", "corr-123");
        assertNotNull(ctx.getTraceId());
        assertNotNull(ctx.getSpanId());
        assertEquals("createorder", ctx.getServiceName());
        assertEquals("corr-123",    ctx.getCorrelationId());
        assertNotNull(ctx.getTimestamp());
        assertTrue(ctx.getTraceId().startsWith("ORD-"));
    }

    @Test
    void createChildContext_shouldReuseParentTraceId() {
        String parentTrace = "ORD-abc123";
        TraceContext child = TraceIdGenerator.createChildContext(parentTrace, "orderprocessing");
        assertEquals(parentTrace,        child.getTraceId());
        assertEquals("orderprocessing",  child.getServiceName());
        assertNotNull(child.getSpanId());
    }

    @Test
    void traceContextHolder_shouldSetAndClear() {
        TraceContext ctx = TraceIdGenerator.createContext("TST", "test-service", null);
        TraceContextHolder.set(ctx);
        assertEquals(ctx.getTraceId(), TraceContextHolder.getTraceId());
        TraceContextHolder.clear();
        assertEquals("UNKNOWN", TraceContextHolder.getTraceId());
    }

    @Test
    void toLogString_shouldContainAllFields() {
        TraceContext ctx = TraceIdGenerator.createContext("ORD", "createorder", "corr-456");
        String log = ctx.toLogString();
        assertTrue(log.contains("traceId="));
        assertTrue(log.contains("spanId="));
        assertTrue(log.contains("correlationId=corr-456"));
        assertTrue(log.contains("service=createorder"));
    }
}
