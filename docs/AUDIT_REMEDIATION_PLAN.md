# Audit & Remediation Plan

**Date**: 2026-05-13  
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

## Remediation Phases

### 🔴 Phase 1 — Critical (Security & Correctness)

| #   | Task                                                      | Files                                                                                       | Status |
|-----|-----------------------------------------------------------|---------------------------------------------------------------------------------------------|--------|
| 1.1 | Replace hardcoded secrets with env var placeholders       | `products/application.yml`, `billing/application.yml`, `docker-compose.yml`, `.env.example` | ⬜      |
| 1.2 | Fix ValidatorFactory resource leak in test                | `CreateProductCommandTest.java`                                                             | ⬜      |
| 1.3 | Add global `@RestControllerAdvice` for HTTP error mapping | New: `products/exception/GlobalExceptionHandler.java`                                       | ⬜      |

### 🟠 Phase 2 — Event Sourcing Completeness

| #   | Task                                                       | Files                              | Status |
|-----|------------------------------------------------------------|------------------------------------|--------|
| 2.1 | Complete `UpdateProductCommandHandler` with Event Sourcing | `UpdateProductCommandHandler.java` | ⬜      |
| 2.2 | Create `ProductUpdatedEvent` domain event                  | New: `ProductUpdatedEvent.java`    | ⬜      |
| 2.3 | Update `DomainEventPublisher` for `ProductUpdatedEvent`    | `DomainEventPublisher.java`        | ⬜      |
| 2.4 | Add Kafka producer for `ProductUpdatedEvent`               | `EventPublisher.java`              | ⬜      |

### 🟠 Phase 3 — Billing Service Completion

| #   | Task                                               | Files                                                    | Status |
|-----|----------------------------------------------------|----------------------------------------------------------|--------|
| 3.1 | Add MongoDB dependency to billing                  | `billing/build.gradle`                                   | ⬜      |
| 3.2 | Configure MongoDB in billing application.yml       | `billing/application.yml`                                | ⬜      |
| 3.3 | Create `InvoiceEntity` and `InvoiceRepository`     | New: 2 files                                             | ⬜      |
| 3.4 | Replace mock implementations with real persistence | `InvoiceCommandHandler.java`, `InvoiceQueryHandler.java` | ⬜      |

### 🟠 Phase 4 — Logging & Robustness

| #   | Task                                                       | Files                  | Status |
|-----|------------------------------------------------------------|------------------------|--------|
| 4.1 | Replace `System.err.println` with `log.error()`            | `EventPublisher.java`  | ⬜      |
| 4.2 | Fix Event deserialization (use FQCN instead of SimpleName) | `MongoEventStore.java` | ⬜      |

### 🟡 Phase 5 — Cleanup

| #   | Task                                                              | Files                                                    | Status |
|-----|-------------------------------------------------------------------|----------------------------------------------------------|--------|
| 5.1 | Remove unused `spring-kafka` from eureka + gateway                | `eureka-server/build.gradle`, `api-gateway/build.gradle` | ⬜      |
| 5.2 | Remove dead `BaseController` classes                              | Both `BaseController.java` files                         | ⬜      |
| 5.3 | Remove unused `domainEvents` from `ProductEntity`                 | `ProductEntity.java`                                     | ⬜      |
| 5.4 | Remove unused import in `ProductCreatedEvent`                     | `ProductCreatedEvent.java`                               | ⬜      |
| 5.5 | Rename `ProductLookupEventsHandler` → `ProductLookupEventHandler` | 1 file rename + references                               | ⬜      |
| 5.6 | Pin Docker image versions                                         | `docker-compose.yml`                                     | ⬜      |
| 5.7 | Update `SECURITY.md` with actual policy                           | `SECURITY.md`                                            | ⬜      |

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

#### 1.2 ValidatorFactory Resource Leak

- `CreateProductCommandTest.java:20-22` — `try-with-resources` closes factory immediately, leaving `validator` field
  referencing a closed factory's validator. All 10 test methods may operate on an invalid validator.

#### 1.3 Missing Global Exception Handler

- `DuplicateProductException` → 500 instead of 409 CONFLICT
- `ProductNotFoundException` → 500 instead of 404 NOT_FOUND
- No `@RestControllerAdvice` or `@ExceptionHandler` in either service.

### 🟠 High

#### 2.1-2.4 Incomplete Update Product Path

- `UpdateProductCommandHandler` directly modifies `ProductEntity` and saves it.
- Does NOT raise `ProductUpdatedEvent`, save to EventStore, or publish to Kafka.
- Breaks Event Sourcing contract and SAGA visibility.

#### 3.1-3.4 Billing Service Mock Data

- `InvoiceCommandHandler` — mock in-memory, no repository
- `InvoiceQueryHandler` — returns hardcoded mock list
- No MongoDB dependency in `billing/build.gradle`
- No persistence config in `billing/application.yml`
- SAGA `ProductCreatedEventHandler.handle()` body is empty (just logging)

#### 4.1 System.err.println

- `EventPublisher.java:34` — uses `System.err.println` instead of a proper logger. Class has Lombok but no `@Slf4j`.

#### 4.2 Event Deserialization Fragility

- `MongoEventStore.java:63` — `Class.forName("com.example.products.domain.event." + entity.getEventType())`
- If an event class is renamed or moved, ALL stored events become unreadable.
- No event versioning or schema evolution strategy.

### 🟡 Medium

- `spring-kafka` declared in `eureka-server/build.gradle:21` and `api-gateway/build.gradle:23` but never used.
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
- `SECURITY.md` is a GitHub template placeholder.

---

## Status Key

| Symbol | Meaning     |
|--------|-------------|
| ⬜      | Pending     |
| 🔄     | In Progress |
| ✅      | Complete    |
| ❌      | Blocked     |
