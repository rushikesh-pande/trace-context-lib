package com.orderprocessing.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

/**
 * GDPR Data Service — Enhancement: GDPR Compliance (Enhancement #6)
 * Right to Access, Right to Erasure (Right to be Forgotten),
 * Data Minimization, Consent Management
 */
@Service @Slf4j
public class GdprDataService {

    // In production these would be persisted to DB
    private final Map<String, List<String>> consentStore  = new HashMap<>();
    private final Map<String, Instant>      deletionQueue = new HashMap<>();

    /**
     * Records user consent for data processing.
     */
    public void recordConsent(String userId, String purpose) {
        consentStore.computeIfAbsent(userId, k -> new ArrayList<>()).add(purpose);
        log.info("[GDPR][CONSENT-GRANTED] userId={} purpose={} time={}", userId, purpose, Instant.now());
    }

    /**
     * Checks if user has consented to a specific purpose.
     */
    public boolean hasConsent(String userId, String purpose) {
        return consentStore.getOrDefault(userId, Collections.emptyList()).contains(purpose);
    }

    /**
     * Right to Erasure — schedules customer data deletion (Art. 17 GDPR).
     */
    public void requestErasure(String userId) {
        deletionQueue.put(userId, Instant.now().plusSeconds(30 * 24 * 3600L)); // 30-day retention
        log.info("[GDPR][ERASURE-REQUESTED] userId={} scheduledDeletion={}", userId, deletionQueue.get(userId));
    }

    /**
     * Right to Data Portability — exports user data as JSON map (Art. 20 GDPR).
     */
    public Map<String, Object> exportUserData(String userId) {
        log.info("[GDPR][DATA-EXPORT] userId={}", userId);
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("userId",      userId);
        export.put("exportTime",  Instant.now().toString());
        export.put("consents",    consentStore.getOrDefault(userId, Collections.emptyList()));
        export.put("gdprVersion", "GDPR-2018");
        return export;
    }

    /**
     * Anonymizes PII fields for reporting/analytics (data minimization).
     */
    public String anonymize(String pii) {
        if (pii == null || pii.length() <= 4) return "***";
        return pii.substring(0, 2) + "***" + pii.substring(pii.length() - 2);
    }

    /**
     * Withdraws consent for a specific purpose.
     */
    public void withdrawConsent(String userId, String purpose) {
        List<String> consents = consentStore.getOrDefault(userId, new ArrayList<>());
        consents.remove(purpose);
        log.info("[GDPR][CONSENT-WITHDRAWN] userId={} purpose={}", userId, purpose);
    }
}
