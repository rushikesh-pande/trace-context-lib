package com.orderprocessing.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Audit Log REST Controller — exposes audit trail to ADMIN role only
 */
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogService auditLogService;

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogService.AuditEvent>> getRecent(
            @RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(auditLogService.getRecentEvents(limit));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.name")
    public ResponseEntity<List<AuditLogService.AuditEvent>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditLogService.getEventsByUser(userId));
    }

    @GetMapping("/violations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogService.AuditEvent>> getViolations() {
        return ResponseEntity.ok(auditLogService.getSecurityViolations());
    }
}
