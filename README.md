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
