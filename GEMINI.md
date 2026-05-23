# GEMINI.md - AI Development Assistant Integration

## 🤖 Overview

This file documents the integration and role of the AI Assistant (Gemini) within the **Enterprise Microservices
Architecture** project. As an expert developer assistant, Gemini provides real-time support for code generation,
architectural review, documentation maintenance, and infrastructure troubleshooting.

## 🚀 Project Knowledge Base

Gemini maintains a deep understanding of this project's specific tech stack and architectural choices:

- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.4.5 & Spring Cloud 2024.0.1
- **Patterns**: Event Sourcing, CQRS, SAGA, Command Bus, and DDD
- **Infrastructure**: MongoDB 8.0, Kafka 3.9.2, Redis 7, HashiCorp Vault, and Netflix Eureka
- **API Strategy**: Contract-first with OpenAPI 3.0 and Swagger UI

## 🧠 Architectural Awareness

Gemini is configured to assist with the following core patterns implemented in this POC:

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

You can interact with Gemini for:

- **Code Generation**: Creating boilerplate for new microservices, DTOs, or repositories.
- **Refactoring**: Improving SOLID compliance or simplifying complex business logic in Aggregates.
- **Documentation**: Automatically updating `.md` files (like this one) based on recent code changes.
- **Troubleshooting**: Debugging Kafka connection issues, Vault secret injection, or Gateway routing errors.
- **API Design**: Reviewing and updating OpenAPI (`.yaml`) specifications.

## 📝 Recent AI Contributions

- **Documentation Review**: Comprehensive analysis and indexing of all project documentation.
- **Kafka Optimization**: Provided configuration insights for `AUTO_OFFSET_RESET_CONFIG`.
- **Architectural Guidance**: Validating the 3-layer validation strategy (API -> Command -> Aggregate).
- **Project Structure**: Assisting in the organization of the `docs/` folder and navigation index.
- **Documentation Overhaul (Phase 5.4)**: README trimmed to landing page, ARCHITECTURE.md with 4 Mermaid diagrams.
- **Logging Polish (Phase 5.5)**: `@Slf4j` added across handlers and infrastructure, `info`→`debug` downgrades, zero
  silent catch blocks.
- **SAGA Flow Fixes (May 2026)**: Fixed deserialization — aligned `ProductEvent` fields with `ProductCreatedEvent`
  JSON (`productId`), registered `JavaTimeModule`. Fixed Kafka Docker networking — containers use `kafka:19092`. Fixed
  gateway routing — added catch-all routes with `RewritePath`.

## 📖 How to Interact

To get the most out of Gemini in this project:

1. **Provide Context**: Mention specific files (e.g., "In `ProductAggregate.java`, implement...")
2. **Reference Patterns**: Use terms like "CQRS", "SAGA", or "Event Store" to ensure architectural alignment.
3. **Ask for Explanations**: Request deep dives into configuration classes like `KafkaConsumerConfig` or `RedisConfig`.
4. **Iterative Refinement**: Ask Gemini to review code changes for SOLID compliance or potential performance
   bottlenecks.

---

**Gemini is your partner in building production-grade distributed systems.** 🚀
