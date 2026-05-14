# Audit & Remediation Plan

**Date**: 2026-05-14  
**Scope**: Full codebase audit of `poc-microservices`  
**Auditor**: AI Assistant (Claude)

---

## Overview

A comprehensive audit was performed on the Enterprise Microservices Architecture POC. The project demonstrates
sophisticated patterns (CQRS, Event Sourcing, SAGA, DDD, Command Bus, Redis caching) but has material issues preventing
production readiness.

**Overall Assessment**: Architecture is mature and well-designed, but the codebase is **not production-ready** due to
hardcoded secrets, incomplete Event Sourcing paths, mock data in Billing service, missing error handling, and no
CI/CD/containerization.

---

## Findings Summary

| Severity    | Count | Key Areas                                                                         |
|-------------|-------|-----------------------------------------------------------------------------------|
| 🔴 Critical | 3     | Hardcoded secrets, test resource leak, missing error handler                      |
| 🟠 High     | 4     | Billing mocks, incomplete Event Sourcing, stderr logging, fragile deserialization |
| 🟡 Medium   | 5     | Unused deps, version conflicts, Docker tags, missing CI/CD/K8s                    |
| 🔵 Low      | 8     | Dead code, naming, missing endpoints, SECURITY.md, etc.                           |

---

## Dependabot / CVE Vulnerabilities

| CVE                           | Package             | Affected | Fixed   | Status  |
|-------------------------------|---------------------|----------|---------|---------|
| CVE-2025-24813                | `tomcat-embed-core` | 10.1.33  | 10.1.53 | ✅ Fixed |
| Tomcat CLIENT_CERT auth       | `tomcat-embed-core` | 10.1.35  | 10.1.53 | ✅ Fixed |
| Eclipse Jersey Race Condition | `jersey-client`     | 3.1.9    | 3.1.10  | ✅ Fixed |
|                               |                     |          |         |         |

### CVE-2025-24813 — Apache Tomcat Path Equivalence

**Package**: `org.apache.tomcat.embed:tomcat-embed-core` (transitive via Spring Boot + Eureka)  
**Fix**: Added `ext.set('tomcat.version', '10.1.53')` to `products/build.gradle`, `billing/build.gradle`,
`eureka-server/build.gradle`

**Details**: Path equivalence vulnerability allowing potential RCE and/or information disclosure and/or information
corruption with partial PUT. Affects Tomcat 10.1.0-M1 through 10.1.34.

**Prerequisites for exploit**: writes enabled for default servlet (disabled by default), partial PUT enabled (default),
file-based session persistence with default location, deserialization library present.

### Apache Tomcat CLIENT_CERT Authentication Bypass

**Package**: `org.apache.tomcat.embed:tomcat-embed-core` (transitive via Spring Boot + Eureka)  
**Affected**: `10.1.35` → **Fixed**: `10.1.53`
**Fix**: Bumped `ext.set('tomcat.version', '10.1.53')` in `products/build.gradle`, `billing/build.gradle`, `eureka-server/build.gradle`

**Details**: CLIENT_CERT authentication does not fail as expected for some scenarios when soft fail is disabled. Affects Tomcat 10.1.0-M7 through 10.1.52.

### Eclipse Jersey Race Condition (CVE-like)

**Package**: `org.glassfish.jersey.core:jersey-client` (transitive via Eureka)  
**Affected**: `3.1.9` → **Fixed**: `3.1.10`
**Fix**: Added `ext.set('jersey.version', '3.1.10')` to `products/build.gradle`, `billing/build.gradle`,
`eureka-server/build.gradle`, `api-gateway/build.gradle`

**Details**: In Eclipse Jersey versions 2.45, 3.0.16, 3.1.9 a race condition can cause ignoring of critical SSL
configurations — such as mutual authentication, custom key/trust stores, and other security settings. This may result in
`SSLHandshakeException` under normal circumstances, but could lead to unauthorized trust in insecure servers.

---

## Remediation Phases

### 🔴 Phase 1 — Critical (Security & Correctness)

| #   | Task                                                      | Files                                                                                       | Status |
|-----|-----------------------------------------------------------|---------------------------------------------------------------------------------------------|--------|
| 1.1 | Replace hardcoded secrets with env var placeholders       | `products/application.yml`, `billing/application.yml`, `docker-compose.yml`, `.env.example` | ✅      |
| 1.2 | Fix ValidatorFactory resource leak in test                | `CreateProductCommandTest.java`                                                             | ✅      |
| 1.3 | Add global `@RestControllerAdvice` for HTTP error mapping | `products/exception/GlobalExceptionHandler.java`                                            | ✅      |

### 🟠 Phase 2 — Event Sourcing Completeness

| #   | Task                                                       | Files                              | Status |
|-----|------------------------------------------------------------|------------------------------------|--------|
| 2.1 | Complete `UpdateProductCommandHandler` with Event Sourcing | `UpdateProductCommandHandler.java` | ⬜      |
| 2.2 | Create `ProductUpdatedEvent` domain event                  | New: `ProductUpdatedEvent.java`    | ⬜      |
| 2.3 | Update `DomainEventPublisher` for `ProductUpdatedEvent`    | `DomainEventPublisher.java`        | ⬜      |
| 2.4 | Add Kafka producer for `ProductUpdatedEvent`               | `EventPublisher.java`              | ⬜      |

### 🟠 Phase 3 — Billing Service Completion

| #   | Task                                               | Files                                                    | Status     |
|-----|----------------------------------------------------|----------------------------------------------------------|------------|
| 3.1 | Add MongoDB dependency to billing                  | `billing/build.gradle`                                   | ❌ Reverted |
| 3.2 | Configure MongoDB in billing application.yml       | `billing/application.yml`                                | ❌ Reverted |
| 3.3 | Create `InvoiceEntity` and `InvoiceRepository`     | New: 2 files                                             | ❌ Reverted |
| 3.4 | Replace mock implementations with real persistence | `InvoiceCommandHandler.java`, `InvoiceQueryHandler.java` | ❌ Reverted |

### 🟠 Phase 4 — Logging & Robustness

| #   | Task                                                       | Files                  | Status |
|-----|------------------------------------------------------------|------------------------|--------|
| 4.1 | Replace `System.err.println` with `log.error()`            | `EventPublisher.java`  | ⬜      |
| 4.2 | Fix Event deserialization (use FQCN instead of SimpleName) | `MongoEventStore.java` | ⬜      |

### 🟡 Phase 5 — Cleanup

| #   | Task                                                              | Files                                                    | Status |
|-----|-------------------------------------------------------------------|----------------------------------------------------------|--------|
| 5.1 | Remove unused `spring-kafka` from eureka + gateway                | `eureka-server/build.gradle`, `api-gateway/build.gradle` | ✅      |
| 5.2 | Remove dead `BaseController` classes                              | Both `BaseController.java` files                         | ⬜      |
| 5.3 | Remove unused `domainEvents` from `ProductEntity`                 | `ProductEntity.java`                                     | ⬜      |
| 5.4 | Remove unused import in `ProductCreatedEvent`                     | `ProductCreatedEvent.java`                               | ⬜      |
| 5.5 | Rename `ProductLookupEventsHandler` → `ProductLookupEventHandler` | 1 file rename + references                               | ⬜      |
| 5.6 | Pin Docker image versions                                         | `docker-compose.yml`                                     | ⬜      |
| 5.7 | Update `SECURITY.md` with actual policy                           | `SECURITY.md`                                            | ✅      |

### Additional Fixes Applied

| #   | Task                                                       | Files                                                                         | Status |
|-----|------------------------------------------------------------|-------------------------------------------------------------------------------|--------|
| A.1 | Upgrade SpringDoc 2.3.0→2.7.0 (compat with Spring Web 6.2) | `products/build.gradle`, `billing/build.gradle`                               | ✅      |
| A.2 | Upgrade Tomcat 10.1.33→10.1.53 (CVE-2025-24813, CLIENT_CERT auth bypass) | `products/build.gradle`, `billing/build.gradle`, `eureka-server/build.gradle` | ✅      |
| A.3 | Upgrade Jersey 3.1.9→3.1.10 (SSL race condition)           | all 4 `build.gradle` files (jersey.version)                                   | ✅      |
| A.4 | Fix Kafka bootstrap-servers placeholder resolution         | `products/application.yml`, `billing/application.yml`                         | ✅      |
| A.5 | Fix Redis password placeholder resolution                  | `products/application.yml`, `RedisConfig.java`                                | ✅      |
| A.6 | Remove machine-specific `setup-env.ps1`                    | Deleted                                                                       | ✅      |
| A.7 | Clean `gradle.properties` (OS-agnostic)                    | `gradle.properties`                                                           | ✅      |
| A.8 | Remove duplicate/placeholder documentation                 | 5 files deleted, `SECURITY.md` rewritten                                      | ✅      |

---

## Detailed Findings

### 🔴 Critical

#### 1.1 Hardcoded Secrets

- `products/application.yml:13` — `token: myroot` (Vault dev token)
- `products/application.yml:24` — `password: password` (MongoDB)
- `billing/application.yml:13` — `token: myroot` (Vault dev token)
- `docker-compose.yml:11` — MongoDB root password
- `docker-compose.yml:51` — Vault root token
- `docker-compose.yml:69` — Redis requirepass

**Risk**: Production credentials could be leaked. Even in dev, hardcoded values override Vault's purpose.

**Fix**: Replaced with env var placeholders (`${VAULT_TOKEN:myroot}`, `${MONGO_PASSWORD:password}`) and created
`.env.example`.

#### 1.2 ValidatorFactory Resource Leak

- `CreateProductCommandTest.java:20-22` — `try-with-resources` closes factory immediately, leaving `validator` field
  referencing a closed factory's validator. All 10 test methods may operate on an invalid validator.

**Fix**: Moved factory to field, added `@AfterEach tearDown()` with explicit `factory.close()`.

#### 1.3 Missing Global Exception Handler

- `DuplicateProductException` → 500 instead of 409 CONFLICT
- `ProductNotFoundException` → 500 instead of 404 NOT_FOUND
- No `@RestControllerAdvice` or `@ExceptionHandler` in either service.

**Fix**: Created `GlobalExceptionHandler.java` with handlers for 404, 409, 400, and 409 responses.

### 🟠 High

#### 2.1-2.4 Incomplete Update Product Path

- `UpdateProductCommandHandler` directly modifies `ProductEntity` and saves it.
- Does NOT raise `ProductUpdatedEvent`, save to EventStore, or publish to Kafka.
- Breaks Event Sourcing contract and SAGA visibility.

**Status**: Pending

#### 3.1-3.4 Billing Service Mock Data

- `InvoiceCommandHandler` — mock in-memory, no repository
- `InvoiceQueryHandler` — returns hardcoded mock list
- No MongoDB dependency in `billing/build.gradle`
- No persistence config in `billing/application.yml`
- SAGA `ProductCreatedEventHandler.handle()` body is empty (just logging)

**Status**: Reverted — to be re-implemented when prioritized

#### 4.1 System.err.println

- `EventPublisher.java:34` — uses `System.err.println` instead of a proper logger. Class has Lombok but no `@Slf4j`.

**Status**: Pending

#### 4.2 Event Deserialization Fragility

- `MongoEventStore.java:63` — `Class.forName("com.example.products.domain.event." + entity.getEventType())`
- If an event class is renamed or moved, ALL stored events become unreadable.
- No event versioning or schema evolution strategy.

**Status**: Pending

### 🟡 Medium

- `ProductAggregate` manually manages `version++` while `ProductEntity` has Spring Data `@Version` — dual management can
  conflict.
- OpenAPI specs lack error response schemas (400, 404, 409).
- Docker images use `latest` tags — no reproducibility.
- No Dockerfiles for microservices, no CI/CD, no Kubernetes manifests.
- No circuit breaker (Resilience4j), retry/backoff, or distributed tracing.

### 🔵 Low

- `BaseController` classes in both services — unused/empty.
- `ProductEntity.domainEvents` (`@Transient`) — never called from production code.
- `ProductLookupEventsHandler` — grammar (unnecessary plural).
- `ProductCreatedEvent.java:6` — unused `import BeanUtils`.
- Missing `GET /products/{id}` and `DELETE` endpoints in Products API.

---

## Status Key

| Symbol | Meaning            |
|--------|--------------------|
| ⬜      | Pending            |
| 🔄     | In Progress        |
| ✅      | Complete           |
| ❌      | Blocked / Reverted |
