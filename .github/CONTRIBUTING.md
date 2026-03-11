# Contributing to Trace Context Lib

Thank you for contributing! Please follow these guidelines.

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker Desktop (for local Kafka / DB)
- Git

### Local Setup
```bash
# Clone the repo
git clone https://github.com/rushikesh-pande/trace-context-lib.git
cd trace-context-lib

# Build and run tests
mvn clean verify

# Start the service
mvn spring-boot:run

# Health check
curl http://localhost:8091/actuator/health
```

### Start local monitoring stack
```bash
cd monitoring
docker-compose -f docker-compose-monitoring.yml up -d
# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3000 (admin / admin123)
# Kibana:     http://localhost:5601
```

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code |
| `develop` | Integration branch |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `hotfix/<name>` | Critical production fixes |

```bash
# Create a feature branch
git checkout -b feature/my-feature develop

# Keep it up to date
git pull --rebase origin develop
```

## Commit Message Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short description>

[optional body]

[optional footer: Closes #123, CODEGEN-456]
```

| Type | When to use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Code restructure |
| `perf` | Performance improvement |
| `test` | Adding tests |
| `ci` | CI/CD changes |
| `monitoring` | Metrics/logging changes |

**Examples:**
```
feat(order): add order cancellation endpoint
fix(kafka): handle null payload in consumer
monitoring(metrics): add payment_amount_total counter
ci(workflow): add OWASP dependency scan
```

## Pull Request Process

1. Create branch from `develop`
2. Make changes following code style
3. Add/update tests (maintain >80% coverage)
4. Add monitoring metrics if new business logic added
5. Run `mvn verify` — all tests must pass
6. Fill in the PR template completely
7. Request review from `@rushikesh-pande`
8. Address review comments
9. Squash commits before merge

## Code Style

- Follow Google Java Style Guide
- Max line length: 120 characters
- Use Lombok for boilerplate reduction
- Add Javadoc to all public methods
- Use structured logging: `log.info("message", key, value)` not string concat

## Adding Business Metrics

When adding new business logic, register metrics:

```java
@Autowired
private BusinessMetricsService metrics;

public Order createOrder(OrderRequest request) {
    metrics.incrementActive();
    try {
        // ... business logic ...
        metrics.recordSuccess("createOrder");
        return order;
    } catch (Exception ex) {
        metrics.recordFailure("createOrder", ex.getClass().getSimpleName());
        throw ex;
    } finally {
        metrics.decrementActive();
    }
}
```

## Testing Standards

| Test type | Location | Tool |
|-----------|----------|------|
| Unit | `src/test/java/**/*Test.java` | JUnit 5 + Mockito |
| Integration | `src/test/java/**/*IT.java` | Spring Boot Test |
| Contract | `src/test/java/**/*ContractTest.java` | Spring Cloud Contract |

Minimum coverage: **80%** for service/controller layers.

## Questions?

Open a [Discussion](https://github.com/rushikesh-pande/trace-context-lib/discussions) or
tag `@rushikesh-pande` in your issue.
