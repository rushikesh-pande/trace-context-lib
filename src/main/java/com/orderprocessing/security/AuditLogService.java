package com.orderprocessing.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Audit Log Service — Enhancement: Audit Logging (Enhancement #7)
 * Tracks all transactions, security events, data access, admin actions
 */
@Service @Slf4j
public class AuditLogService {

    private final Deque<AuditEvent> auditLog = new ConcurrentLinkedDeque<>();
    private static final int MAX_IN_MEMORY = 10_000;

    public enum EventType {
        AUTH_SUCCESS, AUTH_FAILURE, ACCESS_DENIED,
        DATA_READ, DATA_WRITE, DATA_DELETE,
        PAYMENT_INITIATED, PAYMENT_SUCCESS, PAYMENT_FAILED,
        ORDER_CREATED, ORDER_MODIFIED, ORDER_CANCELLED,
        RATE_LIMIT_EXCEEDED, SECURITY_VIOLATION,
        GDPR_CONSENT, GDPR_ERASURE, GDPR_EXPORT,
        ADMIN_ACTION, CONFIGURATION_CHANGE
    }

    public record AuditEvent(
        String    eventId,
        EventType eventType,
        String    userId,
        String    action,
        String    resourceId,
        String    ipAddress,
        String    userAgent,
        boolean   success,
        String    details,
        Instant   timestamp
    ) {}

    public AuditEvent log(EventType type, String userId, String action,
                          String resourceId, String ip, boolean success, String details) {
        AuditEvent event = new AuditEvent(
            UUID.randomUUID().toString(), type, userId, action,
            resourceId, ip, "API", success, details, Instant.now());
        if (auditLog.size() >= MAX_IN_MEMORY) auditLog.pollFirst();
        auditLog.addLast(event);
        String level = success ? "INFO" : "WARN";
        log.atLevel(success ? org.slf4j.event.Level.INFO : org.slf4j.event.Level.WARN)
           .log("[AUDIT] id={} type={} user={} action={} resource={} ip={} success={} details={}",
                event.eventId(), type, userId, action, resourceId, ip, success, details);
        return event;
    }

    public List<AuditEvent> getRecentEvents(int limit) {
        List<AuditEvent> events = new ArrayList<>(auditLog);
        Collections.reverse(events);
        return events.stream().limit(limit).toList();
    }

    public List<AuditEvent> getEventsByUser(String userId) {
        return auditLog.stream().filter(e -> userId.equals(e.userId())).toList();
    }

    public List<AuditEvent> getSecurityViolations() {
        return auditLog.stream()
            .filter(e -> !e.success() || e.eventType()==EventType.SECURITY_VIOLATION
                      || e.eventType()==EventType.ACCESS_DENIED
                      || e.eventType()==EventType.RATE_LIMIT_EXCEEDED)
            .toList();
    }
}
