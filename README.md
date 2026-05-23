# Enterprise Microservices Architecture

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-grade POC demonstrating **Event Sourcing**, **CQRS**, and **SAGA** patterns with Spring Cloud, Kafka,
MongoDB, and Redis.

---

## Quick Links

| Guide                                     | Description                               |
|-------------------------------------------|-------------------------------------------|
| [Quick Start](docs/QUICKSTART.md)         | Get running in 5 minutes                  |
| [Architecture](docs/ARCHITECTURE.md)      | System design, patterns, diagrams         |
| [Docker Setup](docs/DOCKER.md)            | Infrastructure + service containerization |
| [ADR Library](docs/adr/)                  | Architecture Decision Records             |
| [Roadmap](docs/PROD_READINESS_ROADMAP.md) | CVE remediation & production readiness    |

## Service Catalog

| Service              | Purpose                                         | Port     |
|----------------------|-------------------------------------------------|----------|
| **Eureka Server**    | Service registry & discovery                    | `:8761`  |
| **API Gateway**      | Centralized routing (Spring Cloud Gateway)      | `:8080`  |
| **Products Service** | Product CRUD, Event Sourcing, CQRS, Redis cache | dynamic  |
| **Billing Service**  | Invoice management, SAGA consumer               | dynamic  |
| **MongoDB**          | Document DB + Event Store                       | `:27017` |
| **Kafka**            | Event streaming for SAGA choreography           | `:9092`  |
| **Redis**            | Distributed query cache                         | `:6379`  |
| **Vault**            | Secrets management                              | `:8200`  |

## Tech Stack

**Core**: Java 21 (Virtual Threads) · Spring Boot 3.4.5 · Spring Cloud 2024.0.1 · Gradle 8.11.1

**Infrastructure**: MongoDB 8.0 · Apache Kafka 3.9.2 · Redis 7 · HashiCorp Vault · Netflix Eureka

**API**: OpenAPI 3.0 (contract-first) · Swagger UI · SpringDoc

## Getting Started

```bash
# Start infrastructure (MongoDB, Kafka, Redis, Vault)
docker-compose up -d

# Build all services
./gradlew clean build -x test

# Start in order (separate terminals):
./gradlew :eureka-server:bootRun
./gradlew :api-gateway:bootRun
./gradlew :products:bootRun
./gradlew :billing:bootRun
```

See [QUICKSTART.md](docs/QUICKSTART.md) for Vault configuration and API verification steps.

## Swagger UI

| Service      | URL                                                    |
|--------------|--------------------------------------------------------|
| Products API | `http://localhost:8080/products/swagger-ui/index.html` |
| Billing API  | `http://localhost:8080/billing/swagger-ui/index.html`  |

## Testing

```bash
# Run all non-Docker tests
./gradlew test

# Run specific service
./gradlew :products:test
```

> Docker-dependent tests are tagged `@Tag("docker")` and excluded from default build.

## Contributing

1. Branch from `main`, follow existing patterns
2. Update OpenAPI specs for API changes
3. Add tests, run `./gradlew test`
4. Submit PR

Pre-push hook runs `./gradlew build` automatically.

---

## Additional Resources

- [Spring Cloud Docs](https://spring.io/projects/spring-cloud)
- [Event Sourcing (Martin Fowler)](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS (Martin Fowler)](https://martinfowler.com/bliki/CQRS.html)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
