# trace-context-lib

Shared Maven library for distributed **TraceId / CorrelationId propagation**
across all Order Processing microservices.

## Features
| Feature | Class |
|---------|-------|
| TraceId generation (UUID-based, service-prefixed) | `TraceIdGenerator` |
| Thread-local context store + SLF4J MDC sync | `TraceContextHolder` |
| HTTP request/response header propagation | `TraceFilter` |
| Kafka message header propagation | `KafkaTraceHeaders` |
| Spring Boot AutoConfiguration | `TraceAutoConfig` |
| Externalized config | `TraceProperties` |

## Maven Dependency
```xml
<dependency>
    <groupId>com.orderprocessing</groupId>
    <artifactId>trace-context-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

## application.properties
```properties
trace.service-name=createorder
trace.service-prefix=ORD
trace.enabled=true
```

## HTTP Headers
| Header | Direction | Description |
|--------|-----------|-------------|
| `X-Trace-Id` | In & Out | Primary distributed trace ID |
| `X-Span-Id` | Out | Per-service hop span ID |
| `X-Correlation-Id` | In & Out | Optional business correlation ID |

## Kafka Headers
Same header names — use `KafkaTraceHeaders.inject()` / `.extract()`.

## Log Pattern (MDC)
```
%d [%X{traceId}] [%X{spanId}] [%X{serviceName}] %-5level %logger{36} - %msg%n
```

## Build & Install Locally
```bash
mvn clean install
```

## 🔒 Security Enhancements

This service implements all 7 security enhancements:

| # | Enhancement | Implementation |
|---|-------------|----------------|
| 1 | **OAuth 2.0 / JWT** | `SecurityConfig.java` — stateless JWT auth, Bearer token validation |
| 2 | **API Rate Limiting** | `RateLimitingFilter.java` — 100 req/min per IP using Bucket4j |
| 3 | **Input Validation** | `InputSanitizer.java` — SQL injection, XSS, command injection prevention |
| 4 | **Data Encryption** | `EncryptionService.java` — AES-256-GCM for sensitive data at rest |
| 5 | **PCI DSS** | `PciDssAuditAspect.java` — Full audit trail for payment operations |
| 6 | **GDPR Compliance** | `GdprDataService.java` — Right to erasure, consent management, data export |
| 7 | **Audit Logging** | `AuditLogService.java` — All transactions logged with user, IP, timestamp |

### Security Endpoints
- `GET /api/v1/audit/recent?limit=100` — Recent audit events (ADMIN only)
- `GET /api/v1/audit/user/{userId}` — User's audit trail (ADMIN or self)
- `GET /api/v1/audit/violations` — Security violations (ADMIN only)

### JWT Authentication
```bash
# Include Bearer token in all requests:
curl -H "Authorization: Bearer <JWT_TOKEN>" http://localhost:8080/api/v1/...
```

### Security Headers Added
- `X-Frame-Options: DENY`
- `X-Content-Type-Options: nosniff`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `X-RateLimit-Remaining: <n>` (on every response)
