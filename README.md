# Enterprise Microservices Architecture - Proof of Concept

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-grade microservices architecture demonstrating Event Sourcing, CQRS, and SAGA patterns using Spring Cloud ecosystem with enterprise-level security and observability.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Design Patterns](#design-patterns)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Monitoring & Observability](#monitoring--observability)
- [Security](#security)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## Overview

This project implements a distributed microservices architecture following industry best practices and enterprise patterns. The system demonstrates:

- **Event Sourcing** for complete audit trails and temporal queries
- **CQRS** for optimized read/write operations
- **SAGA Pattern** for distributed transaction management
- **Service Discovery** with Netflix Eureka
- **API Gateway** for centralized routing and security
- **Secret Management** with HashiCorp Vault
- **Event Streaming** with Apache Kafka

### Key Features

- ✅ Contract-first API development with OpenAPI 3.0
- ✅ Horizontal scalability with dynamic port allocation
- ✅ Centralized configuration and secret management
- ✅ Comprehensive health monitoring and metrics
- ✅ Interactive API documentation with Swagger UI
- ✅ Event-driven architecture with eventual consistency
- ✅ Domain-Driven Design with rich aggregates
- ✅ Zero-downtime deployments support

## Architecture

### System Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │  Eureka Server  │    │     Kafka       │    │     Vault       │
│   (Port 8080)   │    │   (Port 8761)   │    │   (Port 9092)   │    │   (Port 8200)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │                       │
         │              ┌────────┴────────┐             │                       │
         │              │                 │             │                       │
         ▼              ▼                 ▼             ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Products API   │    │  Billing API    │    │    MongoDB      │    │   Secrets       │
│  (Dynamic Port) │    │  (Dynamic Port) │    │  (Port 27017)   │    │  Management     │
│                 │    │                 │    │                 │    │                 │
│ • Event Store   │    │ • Kafka         │    │ • Products DB   │    │ • Credentials   │
│ • CQRS          │    │   Consumer      │    │ • Event Store   │    │ • Configuration │
│ • Command Bus   │    │ • SAGA Pattern  │    │                 │    │                 │
│ • Aggregates    │    │                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Service Catalog

| Service | Purpose | Port | Technology |
|---------|---------|------|------------|
| **Eureka Server** | Service registry and discovery | 8761 | Netflix Eureka |
| **API Gateway** | Centralized routing, load balancing | 8080 | Spring Cloud Gateway |
| **Products Service** | Product management with Event Sourcing | Dynamic | Spring Boot, MongoDB |
| **Billing Service** | Invoice management and event consumption | Dynamic | Spring Boot, Kafka |
| **MongoDB** | Document database and event store | 27017 | MongoDB 8.0 |
| **Kafka** | Event streaming platform | 9092 | Apache Kafka 4.0 |
| **Vault** | Secret and configuration management | 8200 | HashiCorp Vault |

## Technology Stack

### Core Framework
- **Java 21** - Latest LTS with modern language features
- **Spring Boot 3.4.0** - Production-ready application framework
- **Spring Cloud 2024.0.0** - Microservices patterns and tools
- **Gradle 8.11.1** - Build automation and dependency management

### Infrastructure
- **Netflix Eureka** - Service discovery and registration
- **Spring Cloud Gateway** - Reactive API gateway with load balancing
- **Apache Kafka 4.0.0** - Distributed event streaming
- **MongoDB 8.0** - NoSQL document database
- **HashiCorp Vault** - Secrets management

### Development Tools
- **OpenAPI 3.0.3** - API specification and contract-first development
- **Swagger UI** - Interactive API documentation
- **Spring Boot Actuator** - Production-ready monitoring
- **Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization with JSR310 support

### Containerization
- **Docker** - Container runtime
- **Docker Compose** - Multi-container orchestration

## Design Patterns

### Event Sourcing

Captures all changes to application state as a sequence of events, providing:
- Complete audit trail
- Temporal queries and event replay
- Debugging and troubleshooting capabilities
- State reconstruction from events

**Implementation**: Products Service with MongoDB Event Store

### CQRS (Command Query Responsibility Segregation)

Separates read and write operations for optimal performance:
- **Commands**: State-changing operations with validation
- **Queries**: Optimized read operations without side effects
- Independent scaling of read/write workloads

**Implementation**: Separate handlers for commands and queries in all services

### SAGA Pattern

Manages distributed transactions across microservices:
- Event choreography for loose coupling
- Eventual consistency model
- Compensating transactions for rollback

**Implementation**: Kafka-based event flow from Products to Billing service

### Domain-Driven Design

- **Aggregates**: Rich domain models with business logic
- **Value Objects**: Immutable domain concepts
- **Domain Events**: Business-meaningful state changes
- **Bounded Contexts**: Clear service boundaries

### Additional Patterns

- **Command Bus**: Decoupled command routing
- **Repository Pattern**: Data access abstraction
- **API Gateway Pattern**: Single entry point
- **Service Registry**: Dynamic service discovery
- **Circuit Breaker**: Fault tolerance (planned)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Docker Desktop or Docker Engine with Docker Compose
- Gradle 8.11.1+ (or use included wrapper)
- 8GB RAM minimum (recommended: 16GB)
- 10GB free disk space

### Quick Start

For a rapid setup, see [QUICKSTART.md](QUICKSTART.md) for a 5-minute guide.

### Detailed Setup

#### 1. Clone Repository

```bash
git clone <repository-url>
cd poc-microservices
```

#### 2. Start Infrastructure Services

```bash
docker-compose up -d
```

Verify all containers are running:

```bash
docker-compose ps
```

Expected output:
- `mongodb-poc` - Running on port 27017
- `kafka-poc` - Running on port 9092
- `vault-poc` - Running on port 8200

#### 3. Configure HashiCorp Vault

```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='myroot'

# Configure Products Service secrets
vault kv put secret/products \
  mongodb.username=admin \
  mongodb.password=password \
  kafka.bootstrap-servers=localhost:9092

# Configure Billing Service secrets
vault kv put secret/billing \
  kafka.bootstrap-servers=localhost:9092
```

Verify configuration:

```bash
vault kv get secret/products
vault kv get secret/billing
```

#### 4. Build Services

```bash
./gradlew clean build -x test
```

#### 5. Start Microservices

Start services in the following order (each in a separate terminal):

**Terminal 1 - Eureka Server:**
```bash
./gradlew :eureka-server:bootRun
```
Wait for: `Started EurekaServerApplication`

**Terminal 2 - API Gateway:**
```bash
./gradlew :api-gateway:bootRun
```
Wait for: `Started ApiGatewayApplication`

**Terminal 3 - Products Service:**
```bash
./gradlew :products:bootRun
```
Wait for: `Started ProductsApplication`

**Terminal 4 - Billing Service:**
```bash
./gradlew :billing:bootRun
```
Wait for: `Started BillingApplication`

#### 6. Verify Deployment

**Check Service Registry:**
```bash
curl http://localhost:8761
```

**Test Products API:**
```bash
# List products
curl http://localhost:8080/products/products

# Create product
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Enterprise Product","price":299.99}'
```

**Test Billing API:**
```bash
curl http://localhost:8080/billing/invoices
```

## API Documentation

### Interactive Documentation

Access Swagger UI through the API Gateway:

- **Products API**: http://localhost:8080/products/swagger-ui/index.html
- **Billing API**: http://localhost:8080/billing/swagger-ui/index.html

### Service Endpoints

#### Products Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | Retrieve all active products |
| POST | `/products` | Create new product (triggers event sourcing) |
| GET | `/health` | Service health check |
| GET | `/actuator/health` | Detailed health information |
| GET | `/actuator/info` | Service metadata |

#### Billing Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/invoices` | Retrieve all invoices |
| POST | `/invoices` | Create new invoice |
| GET | `/health` | Service health check |
| GET | `/actuator/health` | Detailed health information |

#### API Gateway

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/gateway/routes` | View configured routes |
| GET | `/actuator/health` | Gateway health status |

### Example Requests

**Create Product:**
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Widget",
    "price": 149.99
  }'
```

**Response:**
```json
{
  "id": 123456789,
  "name": "Premium Widget",
  "price": 149.99
}
```

## Monitoring & Observability

### Service Discovery Dashboard

**Eureka Dashboard**: http://localhost:8761

View registered services, instance health, and metadata.

### Health Endpoints

All services expose Spring Boot Actuator endpoints:

```bash
# Products Service health
curl http://localhost:8080/products/actuator/health

# Billing Service health
curl http://localhost:8080/billing/actuator/health

# Gateway health
curl http://localhost:8080/actuator/health
```

### Metrics

Access detailed metrics at `/actuator/metrics` endpoints:

```bash
curl http://localhost:8080/products/actuator/metrics
```

### Database Monitoring

#### MongoDB Access

**Read-Only User (Recommended):**
```bash
docker exec -it mongodb-poc mongosh \
  -u viewer \
  -p viewonly123 \
  --authenticationDatabase products \
  products
```

**View Products Collection:**
```bash
docker exec -it mongodb-poc mongosh \
  -u viewer \
  -p viewonly123 \
  --authenticationDatabase products \
  products \
  --eval "db.products.find().pretty()"
```

**View Event Store:**
```bash
docker exec -it mongodb-poc mongosh \
  -u viewer \
  -p viewonly123 \
  --authenticationDatabase products \
  products \
  --eval "db.event_store.find().pretty()"
```

### Event Streaming Monitoring

**List Kafka Topics:**
```bash
docker exec -it kafka-poc kafka-topics \
  --list \
  --bootstrap-server localhost:9092
```

**Consume Product Events:**
```bash
docker exec -it kafka-poc kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic product-events \
  --from-beginning
```

## Security

### Secret Management

All sensitive credentials are stored in HashiCorp Vault:

- Database credentials
- Kafka connection strings
- API keys and tokens

**Vault UI**: http://localhost:8200 (Token: `myroot`)

### MongoDB Security

**Admin User:**
- Username: `admin`
- Password: Stored in Vault (`secret/products/mongodb.password`)
- Permissions: Full administrative access
- Authentication Database: `admin`

**Read-Only User:**
- Username: `viewer`
- Password: `viewonly123`
- Permissions: Read-only access to `products` database
- Authentication Database: `products`

### Best Practices

- ✅ No hardcoded credentials in source code
- ✅ Secrets injected at runtime from Vault
- ✅ Principle of least privilege for database users
- ✅ Separate authentication databases
- ✅ Token-based Vault authentication

## Troubleshooting

### Gateway Connection Errors

**Symptom**: API Gateway returns 500 errors or connection refused

**Cause**: Gateway has cached stale service instances after service restarts

**Solution**:
```bash
pkill -f "api-gateway:bootRun"
./gradlew :api-gateway:bootRun
```

### Swagger UI Configuration Issues

**Symptom**: "Failed to load remote configuration" in Swagger UI

**Cause**: Forward headers not properly configured

**Solution**:
1. Verify `forward-headers-strategy: framework` in `application.yml`
2. Restart both service and gateway:
```bash
./gradlew clean build -x test
# Restart services in order: Eureka → Gateway → Products → Billing
```

### MongoDB Connection Failures

**Symptom**: Services fail to connect to MongoDB

**Diagnosis**:
```bash
# Check MongoDB container
docker ps | grep mongodb

# Verify credentials in Vault
vault kv get secret/products
```

**Solution**: Ensure MongoDB container is running and credentials match Vault configuration

### Kafka Connection Issues

**Symptom**: Services cannot publish/consume events

**Diagnosis**:
```bash
# Check Kafka container
docker ps | grep kafka

# Verify Kafka configuration
vault kv get secret/products
vault kv get secret/billing
```

**Solution**: Verify Kafka container is running and bootstrap servers are correctly configured

### Port Conflicts

**Symptom**: Service fails to start due to port already in use

**Solution**:
```bash
# Find process using port
lsof -ti:8080 | xargs kill -9

# Or use dynamic ports (already configured for Products/Billing)
```

### Clean Restart

For persistent issues, perform a clean restart:

```bash
# Stop all services (Ctrl+C in each terminal)

# Stop infrastructure
docker-compose down

# Clean build
./gradlew clean build -x test

# Restart infrastructure
docker-compose up -d

# Restart services in order
```

## Project Structure

```
poc-microservices/
├── eureka-server/              # Service discovery
├── api-gateway/                # API gateway and routing
├── products/                   # Products microservice
│   ├── src/main/resources/openapi/
│   │   └── products-api.yaml   # API specification
│   └── src/main/java/com/example/products/
│       ├── controller/         # REST controllers
│       ├── query/             # Query handlers (CQRS)
│       ├── command/           # Command handlers (CQRS)
│       ├── domain/            # Domain entities & repositories
│       ├── infrastructure/    # Config, mappers, integrations
│       └── model/             # Generated API models
├── billing/                    # Billing microservice
│   ├── src/main/resources/openapi/
│   │   └── billing-api.yaml    # API specification
│   └── src/main/java/com/example/billing/
│       ├── controller/         # REST controllers
│       ├── query/             # Query handlers (CQRS)
│       ├── command/           # Command handlers (CQRS)
│       ├── domain/            # Domain entities
│       ├── enums/             # Enum definitions
│       └── model/             # Generated API models
├── docker-compose.yml          # Infrastructure services
├── QUICKSTART.md              # Quick start guide
├── ARCHITECTURE.md            # Detailed architecture
├── README.md                  # This file
└── gradle/                    # Gradle wrapper
```

## Contributing

### Development Workflow

1. Create feature branch from `main`
2. Implement changes following existing patterns
3. Update OpenAPI specifications for API changes
4. Add/update tests
5. Update documentation
6. Submit pull request

### Code Standards

- Follow Spring Boot best practices
- Maintain SOLID principles
- Use Lombok for boilerplate reduction
- Document public APIs with JavaDoc
- Write meaningful commit messages

### Testing

```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :products:test

# Generate test coverage report
./gradlew jacocoTestReport
```

## Additional Resources

- [Quick Start Guide](QUICKSTART.md) - Get running in 5 minutes
- [Architecture Documentation](ARCHITECTURE.md) - Detailed technical architecture
- [Docker Setup](DOCKER.md) - Infrastructure configuration
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

Built with modern microservices patterns and enterprise-grade technologies:
- Spring Cloud ecosystem
- Netflix OSS components
- Apache Kafka
- MongoDB
- HashiCorp Vault

---

**Enterprise Microservices Architecture** - Production-ready patterns for distributed systems
