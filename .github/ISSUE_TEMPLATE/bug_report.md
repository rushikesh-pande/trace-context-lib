---
name: Bug Report
about: Report a bug in Trace Context Lib
title: '[BUG] <short description>'
labels: ['bug', 'needs-triage']
assignees: 'rushikesh-pande'
---

## Bug Description
<!-- A clear and concise description of the bug -->

## Service
**Service**: `trace-context-lib`
**Version**: <!-- e.g. v1.2.3 or commit SHA -->
**Environment**: <!-- local / staging / production -->

## Steps to Reproduce
1. Call endpoint `...`
2. With payload `...`
3. See error

## Expected Behaviour
<!-- What should have happened -->

## Actual Behaviour
<!-- What actually happened -->

## Error Output
```
<!-- Paste stack trace / log output here -->
```

## Request Details
| Field | Value |
|-------|-------|
| Endpoint | `GET/POST /api/v1/...` |
| HTTP Status | `500` |
| Correlation ID | `X-Correlation-ID: ...` |
| Trace ID | `X-Trace-ID: ...` |

## Environment Details
| Field | Value |
|-------|-------|
| Java Version | 17 |
| Spring Boot | 3.2.2 |
| OS | |
| Docker | Yes / No |

## Monitoring Evidence
<!-- Paste relevant Grafana screenshot, Prometheus metric, or Kibana log query -->

## Possible Fix
<!-- Optional: suggest a fix or link to relevant code -->

## Additional Context
<!-- Any other context about the problem -->
