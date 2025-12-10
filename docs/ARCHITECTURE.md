# Architecture Documentation

## Overview

This microservices architecture implements Event Sourcing, CQRS, and SAGA patterns using Spring Cloud, MongoDB, and Kafka.

## Core Patterns

### 1. Event Sourcing

**Implementation**: Products Service

Event Sourcing captures all changes to application state as a sequence of events. Instead of storing just the current state, we store the full history of state changes.

**Components**:
- **DomainEvent Interface**: Base interface for all domain events
- **ProductCreatedEvent**: Captures product creation with all relevant data
- **EventStore**: MongoDB-based persistence for events
- **EventStoreEntity**: MongoDB document storing event metadata and data

**Flow**:
```
1. Command received → CreateProductCommand
2. Aggregate processes command → ProductAggregate
3. Domain event raised → ProductCreatedEvent
4. Event persisted → EventStore (MongoDB)
5. Event published → Kafka
6. State updated → ProductEntity (MongoDB)
```

**Benefits**:
- Complete audit trail
- Event replay capability
- Temporal queries
- Debug and troubleshooting

### 2. CQRS (Command Query Responsibility Segregation)

**Implementation**: Products and Billing Services

Separates read and write operations into different models.

**Command Side** (Write):
- `CreateProductCommand`
- `CreateProductCommandHandler`
- Validates and processes state changes
- Raises domain events

**Query Side** (Read):
- `ProductQueryHandler`
- Optimized for read operations
- Returns DTOs/Models
- No business logic

**Benefits**:
- Independent scaling of reads/writes
- Optimized data models for each operation
- Clear separation of concerns

### 3. Command Bus Pattern

**Implementation**: Products Service

Routes commands to their appropriate handlers.

**Components**:
- `Command` interface: Marker for all commands
- `CommandHandler<T, R>` interface: Generic handler contract
- `CommandBus`: Routes commands to registered handlers

**Flow**:
```
Controller → CommandBus.dispatch(command) → Handler.handle(command) → Result
```

**Benefits**:
- Decouples command execution from handling
- Easy to add new commands
- Testable handlers

### 4. Domain Aggregates

**Implementation**: ProductAggregate

Rich domain models that encapsulate business logic and enforce invariants.

**Features**:
- Business validation (name length, price range)
- State management (active/inactive)
- Domain event generation
- Invariant enforcement

**Example**:
```java
public void applyDiscount(Double discountPercentage) {
    if (!this.active) {
        throw new IllegalStateException("Cannot apply discount to inactive product");
    }
    // Business logic...
}
```

### 5. SAGA Pattern

**Implementation**: Products → Kafka → Billing

Manages distributed transactions across microservices using event choreography.

**Flow**:
```
1. Products Service creates product
2. ProductCreatedEvent published to Kafka
3. Billing Service consumes event
4. Billing Service creates invoice (future)
5. InvoiceCreatedEvent published (future)
```

**Benefits**:
- Eventual consistency
- Loose coupling
- Resilience to failures

## Technology Stack

### Core Framework
- **Spring Boot 3.4.0**: Application framework
- **Spring Cloud 2024.0.0**: Microservices patterns
- **Java 21**: Modern Java features

### Service Discovery
- **Eureka Server**: Service registry
- **Eureka Client**: Service registration and discovery

### API Gateway
- **Spring Cloud Gateway**: Reactive gateway
- **Load Balancing**: Client-side with Ribbon
- **Routing**: Path-based routing with filters

### Data Persistence
- **MongoDB 8.0**: Document database
  - Products collection: Current state
  - Event Store collection: Event history
- **Spring Data MongoDB**: Repository abstraction

### Event Streaming
- **Apache Kafka 4.0.0**: Event bus
- **Spring Kafka**: Kafka integration
- **Topics**: product-events, invoice-events

### Secret Management
- **HashiCorp Vault**: Centralized secrets
- **Spring Cloud Vault**: Vault integration
- **KV Secrets Engine**: Key-value storage

### API Documentation
- **OpenAPI 3.0.3**: API specification
- **Swagger UI**: Interactive documentation
- **SpringDoc**: OpenAPI integration

## Service Architecture

### Products Service

```
┌─────────────────────────────────────────────────────────┐
│                    Products Service                      │
├─────────────────────────────────────────────────────────┤
│  Controller Layer                                        │
│  ├─ ProductsController (REST endpoints)                 │
│  └─ Implements ProductsApi (OpenAPI generated)          │
├─────────────────────────────────────────────────────────┤
│  Command Layer (CQRS Write)                             │
│  ├─ CommandBus (routes commands)                        │
│  ├─ CreateProductCommandHandler                         │
│  └─ CreateProductCommand                                │
├─────────────────────────────────────────────────────────┤
│  Query Layer (CQRS Read)                                │
│  └─ ProductQueryHandler                                 │
├─────────────────────────────────────────────────────────┤
│  Domain Layer                                            │
│  ├─ ProductAggregate (business logic)                   │
│  ├─ ProductEntity (persistence)                         │
│  ├─ ProductRepository (data access)                     │
│  └─ DomainEvent (event interface)                       │
├─────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                    │
│  ├─ EventStore (event persistence)                      │
│  ├─ EventPublisher (Kafka producer)                     │
│  ├─ ProductMapper (entity ↔ model)                      │
│  └─ ProductAggregateMapper (aggregate ↔ entity)         │
└─────────────────────────────────────────────────────────┘
```

### Event Store Schema

**MongoDB Collection**: `event_store`

```json
{
  "_id": "ObjectId",
  "aggregateId": "68f2bdb949b01c4c50da346f",
  "eventType": "ProductCreatedEvent",
  "eventData": "{\"productId\":\"...\",\"name\":\"...\",\"price\":199.99,...}",
  "version": 0,
  "occurredAt": "ISODate",
  "storedAt": "ISODate"
}
```

### Products Collection Schema

**MongoDB Collection**: `products`

```json
{
  "_id": "ObjectId",
  "name": "Product Name",
  "price": 99.99,
  "description": "Description",
  "category": "Category",
  "active": true,
  "created_at": "ISODate",
  "updated_at": "ISODate",
  "version": 0
}
```

## Configuration Management

### Vault Secret Structure

```
vault/
├── secret/products/
│   ├── mongodb.username = "admin"
│   ├── mongodb.password = "password"
│   └── kafka.bootstrap-servers = "localhost:9092"
└── secret/billing/
    └── kafka.bootstrap-servers = "localhost:9092"
```

### Application Configuration

**Products Service** (`application.yml`):
```yaml
server:
  port: 0  # Random port for scaling
  forward-headers-strategy: framework  # Gateway support

spring:
  application:
    name: products
  config:
    import: vault://  # Load secrets from Vault
  data:
    mongodb:
      host: localhost
      port: 27017
      database: products
      username: ${mongodb.username}
      password: ${mongodb.password}
  kafka:
    bootstrap-servers: ${kafka.bootstrap-servers}
```

## API Gateway Routing

### Route Configuration

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: products-swagger-ui
          uri: lb://products
          predicates:
            - Path=/products/swagger-ui.html,/products/swagger-ui/**
          filters:
            - RewritePath=/products/(?<segment>.*), /${segment}
```

### Load Balancing

- **Strategy**: Round-robin
- **Discovery**: Eureka-based
- **Health Checks**: Actuator endpoints

## Security

### MongoDB Users

**Admin User**:
- Username: `admin`
- Password: Stored in Vault
- Permissions: Full access
- Auth DB: `admin`

**Read-Only User**:
- Username: `viewer`
- Password: `viewonly123`
- Permissions: Read-only on `products` database
- Auth DB: `products`

### Vault Integration

- **Token**: `myroot` (development only)
- **Backend**: KV v2
- **Auto-renewal**: Enabled
- **Lease Duration**: Default

## Monitoring & Observability

### Actuator Endpoints

**Products Service**:
- `/actuator/health` - Health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Metrics

**API Gateway**:
- `/actuator/gateway/routes` - Route information
- `/actuator/health` - Gateway health

### Eureka Dashboard

- **URL**: http://localhost:8761
- **Features**:
  - Service registration status
  - Instance health
  - Metadata viewing

## Development Workflow

### 1. Start Infrastructure

```bash
docker-compose up -d
```

### 2. Configure Vault

```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='myroot'
vault kv put secret/products \
  mongodb.username=admin \
  mongodb.password=password \
  kafka.bootstrap-servers=localhost:9092
```

### 3. Build Services

```bash
./gradlew clean build -x test
```

### 4. Start Services (in order)

```bash
# Terminal 1: Eureka
./gradlew :eureka-server:bootRun

# Terminal 2: Gateway
./gradlew :api-gateway:bootRun

# Terminal 3: Products
./gradlew :products:bootRun

# Terminal 4: Billing
./gradlew :billing:bootRun
```

### 5. Verify Deployment

```bash
# Check Eureka
curl http://localhost:8761

# Test Products API
curl http://localhost:8080/products/products

# Create Product
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":99.99}'
```

## Best Practices

### Event Sourcing
- ✅ Events are immutable
- ✅ Events capture intent (ProductCreated, not ProductUpdated)
- ✅ Store complete event data
- ✅ Version events for schema evolution

### CQRS
- ✅ Separate models for reads and writes
- ✅ Optimize queries independently
- ✅ Use DTOs for API responses
- ✅ Keep command handlers focused

### Aggregates
- ✅ Enforce business invariants
- ✅ Keep aggregates small
- ✅ Use value objects
- ✅ Raise domain events

### Microservices
- ✅ One database per service
- ✅ Async communication via events
- ✅ Independent deployment
- ✅ Service discovery

## Future Enhancements

- [ ] Event replay functionality
- [ ] Snapshot support for aggregates
- [ ] Distributed tracing (Zipkin/Jaeger)
- [ ] Circuit breakers (Resilience4j)
- [ ] API versioning
- [ ] Rate limiting
- [ ] Caching layer (Redis)
- [ ] Kubernetes deployment
- [ ] Monitoring (Prometheus/Grafana)

## References

- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [SAGA Pattern](https://microservices.io/patterns/data/saga.html)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [MongoDB Event Store](https://www.mongodb.com/blog/post/event-sourcing-with-mongodb)
