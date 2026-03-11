# Security Policy — Trace Context Lib

## Supported Versions

| Version | Supported |
|---------|-----------|
| Latest (`main`) | ✅ |
| Previous minor  | ✅ (critical fixes only) |
| Older versions  | ❌ |

## Reporting a Vulnerability

**Please do NOT open a public GitHub issue for security vulnerabilities.**

### Private Reporting (Preferred)
Use GitHub's private vulnerability reporting:
👉 https://github.com/rushikesh-pande/trace-context-lib/security/advisories/new

### Email
Send details to: **security@yourcompany.com**

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (optional)

## Response Timeline

| Step | Timeframe |
|------|-----------|
| Acknowledgement | Within 48 hours |
| Initial assessment | Within 5 business days |
| Fix / patch | Within 30 days (critical: 7 days) |
| Public disclosure | After fix is released |

## Security Best Practices in This Service

- **JWT Authentication**: All endpoints require valid JWT token
- **Rate Limiting**: 100 requests/minute per client (Bucket4j)
- **Input Validation**: All inputs validated with Bean Validation
- **Data Encryption**: Sensitive fields encrypted at rest (Jasypt)
- **Audit Logging**: All transactions logged with correlation ID
- **Dependency Scanning**: Weekly OWASP NVD scans via GitHub Actions
- **Secret Management**: No secrets in code — use environment variables

## Known Security Controls

| Control | Implementation |
|---------|---------------|
| AuthN | Spring Security + JWT |
| AuthZ | Role-based (`ADMIN`, `USER`, `SERVICE`) |
| Transport | HTTPS (TLS 1.2+) |
| Secrets | Jasypt encryption + env vars |
| Logging | No PII in logs, card numbers masked |
| Dependencies | Dependabot + OWASP weekly scan |
