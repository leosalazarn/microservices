# CLAUDE.md - AI Development Assistant Integration

## 🤖 Overview

This file documents the integration and role of the AI Assistant (Claude) within the **Enterprise Microservices
Architecture** project. As an expert developer assistant, Claude provides real-time support for code generation,
architectural review, documentation maintenance, and infrastructure troubleshooting.

## 🚀 Project Knowledge Base

Claude maintains a deep understanding of this project's specific tech stack and architectural choices:

- **Language**: Java 21 (LTS) with Virtual Threads (ADR-003)
- **Framework**: Spring Boot 3.4.5 & Spring Cloud 2024.0.1
- **Patterns**: Event Sourcing, CQRS, SAGA, Command Bus, and DDD
- **Infrastructure**: MongoDB 8.0, Kafka 3.9.2, Redis 7, HashiCorp Vault, and Netflix Eureka
- **API Strategy**: Contract-first with OpenAPI 3.0 and Swagger UI

## 🧠 Architectural Awareness

Claude is configured to assist with the following core patterns implemented in this POC:

### 1. CQRS & Command Bus

- Generation of Command Handlers and Query Handlers.
- Refactoring controllers to maintain separation of concerns.
- Troubleshooting the `CommandBus` dispatching logic and interceptors.

### 2. Event Sourcing

- Modeling Domain Events (e.g., `ProductCreatedEvent`).
- Assisting with MongoDB Event Store implementation and state reconstruction.
- Ensuring event immutability and versioning.

### 3. SAGA Pattern (Choreography)

- Configuring Kafka Producers and Consumers.
- Designing event-driven flows between `Products` and `Billing` services.
- Handling distributed transaction consistency.

### 4. Distributed Caching (Redis)

- Implementing `@Cacheable` and `@CacheEvict` strategies.
- Designing event-driven cache invalidation to maintain loose coupling.
- Monitoring Redis performance via CLI commands.

## 🛠️ Development Workflows

You can interact with Claude for:

- **Code Generation**: Creating boilerplate for new microservices, DTOs, or repositories.
- **Refactoring**: Improving SOLID compliance or simplifying complex business logic in Aggregates.
- **Documentation**: Automatically updating `.md` files based on recent code changes.
- **Troubleshooting**: Debugging Kafka connection issues, Vault secret injection, or Gateway routing errors.
- **API Design**: Reviewing and updating OpenAPI (`.yaml`) specifications.

## 📝 Recent AI Contributions

- **Phase 5.1 (Billing Persistence)**: MongoDB entities, repository, rewritten handlers with real MongoDB, wired SAGA
  consumer.
- **Phase 5.3 (Multi-stage Dockerfiles)**: Dockerfiles for all 4 services with `.dockerignore`, updated
  `docker-compose.yml`.
- **Phase 5.4 (Documentation Overhaul)**: README → landing page (98 lines), ARCHITECTURE.md with 4 Mermaid diagrams (C4
  context, CQRS, SAGA, cache invalidation).
- **Phase 5.5 (Logging Polish)**: Added `@Slf4j` to 8 classes (CommandBus, MongoEventStore, DomainEventPublisher,
  GlobalExceptionHandler, Create/UpdateProductCommandHandler, InvoiceCommandHandler, InvoiceQueryHandler), corrected 4
  `info`→`debug` downgrades. Zero silent catch blocks remain.
- **SAGA Flow Fixes (May 2026)**: Fixed SAGA deserialization — aligned `ProductEvent` fields with `ProductCreatedEvent`
  JSON (`productId`, `@JsonIgnoreProperties`), registered `JavaTimeModule` in billing `ObjectMapper`. Fixed Kafka Docker
  networking — containers now connect to `kafka:19092` (internal `PLAINTEXT`) instead of `kafka:9092` (`PLAINTEXT_HOST`
  advertises `localhost`). Fixed gateway routing — added catch-all `products-api`/`billing-api` routes with
  `RewritePath`. Fixed `optional:vault://` imports and Redis env var names (`SPRING_DATA_REDIS_*`). SAGA end-to-end
  verified: product creation → Kafka event → billing invoice creation.
- **Pre-push Git Hook**: Auto-detects `JAVA_HOME` from `$HOME/.jdks/`, runs `./gradlew build` before every push.
- **Documentation Indexing**: Assisted in the comprehensive review and indexing of all project documentation.
- **Kafka Configuration**: Refined Kafka consumer settings like `AUTO_OFFSET_RESET_CONFIG`.
- **Architectural Mapping**: Validated the 3-layer validation strategy and Command Bus implementation.
- **Assistant Integration**: Documenting the role of AI partners (Gemini and Claude) in the development lifecycle.

## 📖 How to Interact

To get the most out of Claude in this project:

1. **Provide Context**: Mention specific files (e.g., "In `ProductAggregate.java`, implement...")
2. **Reference Patterns**: Use terms like "CQRS", "SAGA", or "Event Store" to ensure architectural alignment.
3. **Ask for Explanations**: Request deep dives into configuration classes like `KafkaConsumerConfig` or `RedisConfig`.
4. **Iterative Refinement**: Ask Claude to review code changes for SOLID compliance or potential performance
   bottlenecks.

---

## 🪝 Git Hooks

A pre-push hook at `.githooks/pre-push` runs `./gradlew build` before every push.
Docker-dependent integration tests (tagged `@Tag("docker")`) are excluded from the default build.

To enable locally:

```bash
git config core.hooksPath .githooks
```

---

**Claude is your partner in building production-grade distributed systems.** 🚀
