# Architecture Documentation

This microservices architecture implements Event Sourcing, CQRS, and SAGA patterns using Spring Cloud, MongoDB, Kafka and Redis.

## Core Patterns

### 1. Event Sourcing

Captures all state changes as a sequence of immutable events instead of storing just the current state.

```
Command → Aggregate processes → Domain event raised → EventStore (MongoDB) → Kafka → Entity updated (projection)
```

**Components**: `DomainEvent` interface, `ProductCreatedEvent`, `ProductUpdatedEvent`, `EventStore`, `EventStoreEntity`

### 2. CQRS (Command Query Responsibility Segregation)

Separates read and write operations into different models.

**Command Side** (Write): `CreateProductCommand` / `UpdateProductCommand` → Handler → Aggregate → Events  
**Query Side** (Read): `ProductQueryHandler` → Repository → Model (cached via Redis)

### 3. Command Bus Pattern

Routes commands to registered handlers with validation and interceptor pipeline.

```
Controller → CommandBus.dispatch(command) → preProcess interceptors → Validation → Handler → postProcess → Result
```

### 4. SAGA Pattern (Choreography)

Distributed transactions via event choreography over Kafka.

```
Products Service → ProductCreatedEvent → Kafka [product-events] → Billing Service → Invoice created
```

### 5. Event-Driven Cache Invalidation

```
Write → DomainEvent → ApplicationEventPublisher → @EventListener → @CacheEvict → Redis cleared
```

## Technology Stack

| Layer                | Technology                                               |
|----------------------|----------------------------------------------------------|
| Framework            | Spring Boot 3.4.5 / Spring Cloud 2024.0.1                |
| Language             | Java 21 (Virtual Threads — ADR-003)                      |
| Service Discovery    | Netflix Eureka                                           |
| API Gateway          | Spring Cloud Gateway                                     |
| Database             | MongoDB 8.0                                              |
| Event Streaming      | Apache Kafka 3.9.2                                       |
| Cache                | Redis 7                                                  |
| Secrets              | HashiCorp Vault                                          |
| API Docs             | OpenAPI 3.0.3 / Swagger UI / SpringDoc                   |

## Service Architecture

### Products Service

```
┌──────────────────────────────────────────────────────────┐
│                    Products Service                       │
├──────────────────────────────────────────────────────────┤
│  Controllers (CQRS separated)                            │
│  ├─ ProductCommandController (POST/PUT — via CommandBus) │
│  └─ ProductQueryController (GET — via QueryHandler)      │
├──────────────────────────────────────────────────────────┤
│  Domain Layer                                             │
│  ├─ ProductAggregate (business rules, versioning)         │
│  ├─ ProductEntity (MongoDB @Document)                    │
│  ├─ EventStore (MongoDB event persistence)               │
│  └─ DomainEvent interface + events                       │
├──────────────────────────────────────────────────────────┤
│  Infrastructure                                          │
│  ├─ EventPublisher (Kafka producer)                      │
│  ├─ DomainEventPublisher (Kafka + ApplicationEventBus)   │
│  ├─ CacheInvalidationEventHandler (@EventListener)       │
│  └─ ProductAggregateMapper, ProductMapper                │
└──────────────────────────────────────────────────────────┘
```

### Billing Service

```
┌──────────────────────────────────────────────────────────┐
│                    Billing Service                        │
├──────────────────────────────────────────────────────────┤
│  Controllers                                              │
│  ├─ InvoiceCommandController (POST /invoices)             │
│  └─ InvoiceQueryController (GET /invoices)               │
├──────────────────────────────────────────────────────────┤
│  Event Consumers                                          │
│  └─ ProductCreatedEventHandler (Kafka → Invoice creation) │
└──────────────────────────────────────────────────────────┘
```

### Eureka Server

Eureka service registry for all microservices.

### API Gateway (Spring Cloud Gateway)

Routes and load-balances requests to services by path prefix (`/products/*`, `/billing/*`).

## Data Flow

```
Client → API Gateway (8080) → Products Service → CommandBus → Aggregate → EventStore (MongoDB)
                                                                         ↓
                                                                     Kafka (product-events)
                                                                         ↓
                                                                   Billing Service (SAGA consumer)
```

## ADRs

See `docs/adr/` for detailed trade-off analyses:
- [ADR-001](adr/ADR-001-cqrs-event-sourcing-over-crud.md): CQRS + Event Sourcing over CRUD
- [ADR-002](adr/ADR-002-choreographed-saga-over-orchestrated.md): Choreographed SAGA
- [ADR-003](adr/ADR-003-virtual-threads-over-reactive.md): Virtual Threads
