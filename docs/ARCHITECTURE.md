# Architecture Documentation

This microservices architecture implements **Event Sourcing**, **CQRS**, and **SAGA** patterns using Spring Cloud,
MongoDB, Kafka, and Redis with Java 21 Virtual Threads.

---

## System Context

```mermaid
graph TB
    USER[("User / HTTP Client")]

    GW["API Gateway :8080"]

    EU["Eureka Server :8761"]

    PS["Products Service"]
    BS["Billing Service"]

    MONGO[("MongoDB :27017")]
    KAFKA["Kafka :9092"]
    REDIS[("Redis :6379")]
    VAULT["Vault :8200"]

    USER -->|HTTP| GW
    GW --> PS
    GW --> BS
    PS --> EU
    BS --> EU
    PS --> MONGO
    PS --> KAFKA
    BS --> KAFKA
    PS --> REDIS
    PS --> VAULT
    BS --> VAULT

    classDef infra fill:#f9f,stroke:#333,stroke-width:2px
    classDef service fill:#bbf,stroke:#333,stroke-width:2px
    classDef client fill:#dfd,stroke:#333,stroke-width:2px
    class MONGO,KAFKA,REDIS,VAULT infra
    class PS,BS,GW,EU service
    class USER client
```

---

## CQRS + Event Sourcing Flow

```mermaid
sequenceDiagram
    participant Client
    participant Gateway
    participant CmdCtrl as "CommandController"
    participant Bus as "CommandBus"
    participant Aggregate as "ProductAggregate"
    participant Store as "EventStore (MongoDB)"
    participant Kafka
    participant QueryCtrl as "QueryController"
    participant Handler as "QueryHandler"
    participant Cache as "Redis Cache"

    Note over Client,Cache: Command (Write) Path
    Client->>Gateway: POST /products
    Gateway->>CmdCtrl: route
    CmdCtrl->>Bus: dispatch(CreateProductCommand)
    Bus->>Bus: validate + preProcess
    Bus->>Aggregate: process(command)
    Aggregate->>Store: save(ProductCreatedEvent)
    Store-->>Aggregate: stored
    Aggregate->>Kafka: publish(event)
    Aggregate-->>Bus: result
    Bus-->>CmdCtrl: result
    CmdCtrl-->>Gateway: 201 Created
    Gateway-->>Client: response

    Note over Client,Cache: Query (Read) Path
    Client->>Gateway: GET /products
    Gateway->>QueryCtrl: route
    QueryCtrl->>Handler: handle(query)
    Handler->>Cache: lookup
    alt Cache hit
        Cache-->>Handler: cached data
    else Cache miss
        Handler->>Store: query database
        Store-->>Handler: results
        Handler->>Cache: store (10min TTL)
    end
    Handler-->>QueryCtrl: result
    QueryCtrl-->>Gateway: response
    Gateway-->>Client: JSON
```

---

## SAGA Choreography (Products → Billing)

```mermaid
sequenceDiagram
    participant Client
    participant Products as "Products Service"
    participant KafkaBroker as "Kafka (product-events)"
    participant Billing as "Billing Service"
    participant InvoiceDB as "MongoDB (Invoices)"

    Client->>Products: POST /products
    Products->>Products: CreateProductCommand
    Products->>Products: save to EventStore
    Products->>KafkaBroker: publish ProductCreatedEvent
    Products-->>Client: 201 Created

    Note over Products,Billing: Eventually consistent

    KafkaBroker-->>Billing: consume ProductCreatedEvent
    Billing->>Billing: ProductCreatedEventHandler.handle()
    Billing->>Billing: CreateInvoiceCommand
    Billing->>InvoiceDB: save InvoiceEntity
    Billing-->>KafkaBroker: (optional) publish InvoiceCreatedEvent

    Note over Billing: SAGA step completed
```

---

## Event-Driven Cache Invalidation

```mermaid
sequenceDiagram
    participant Client
    participant CmdCtrl as "CommandController"
    participant Bus as "CommandBus"
    participant Aggregate as "ProductAggregate"
    participant Pub as "DomainEventPublisher"
    participant Listener as "CacheInvalidationEventHandler"
    participant Redis

    Client->>CmdCtrl: POST /products
    CmdCtrl->>Bus: dispatch(CreateProductCommand)
    Bus->>Aggregate: process(command)
    Aggregate->>Pub: publish event
    Pub->>Pub: Kafka producer
    Pub->>Listener: ApplicationEventPublisher
    Listener->>Redis: @CacheEvict("products")
    Note over Redis: products::all cleared
    Aggregate-->>Bus: result
    Bus-->>CmdCtrl: result
    CmdCtrl-->>Client: response

    Note over Client,Redis: Next GET rebuilds cache
```

---

## Core Patterns

### Event Sourcing

Captures all state changes as immutable events instead of storing just current state.

**Components**: `DomainEvent` interface, `ProductCreatedEvent`, `ProductUpdatedEvent`, `EventStore`, `EventStoreEntity`

### CQRS (Command Query Responsibility Segregation)

- **Command Side** (Write): `CreateProductCommand` / `UpdateProductCommand` → Handler → Aggregate → Events
- **Query Side** (Read): `ProductQueryHandler` → Repository → Model (cached via Redis)

### Command Bus

Routes commands to registered handlers with validation and interceptor pipeline:

```
Controller → CommandBus.dispatch(command) → preProcess → Validation → Handler → postProcess → Result
```

### SAGA (Choreography)

Distributed transactions via event choreography over Kafka:

- Products publishes `ProductCreatedEvent` → Kafka `product-events` topic
- Billing consumes event → creates `Invoice`

### Event-Driven Cache Invalidation

```
Write → DomainEvent → ApplicationEventPublisher → @EventListener → @CacheEvict → Redis cleared
```

> **Note**: In-process `@EventListener` works for single-instance POC. Multi-instance production needs Kafka/Redis
> Pub/Sub for cross-instance eviction.

---

## Tech Stack

| Layer             | Technology                                |
|-------------------|-------------------------------------------|
| Framework         | Spring Boot 3.4.5 / Spring Cloud 2024.0.1 |
| Language          | Java 21 (Virtual Threads — ADR-003)       |
| Service Discovery | Netflix Eureka                            |
| API Gateway       | Spring Cloud Gateway                      |
| Database          | MongoDB 8.0                               |
| Event Streaming   | Apache Kafka 3.9.2                        |
| Cache             | Redis 7                                   |
| Secrets           | HashiCorp Vault                           |
| API Docs          | OpenAPI 3.0.3 / Swagger UI / SpringDoc    |

## ADRs

See `docs/adr/` for Architecture Decision Records:

- [ADR-001](adr/ADR-001-cqrs-event-sourcing-over-crud.md): CQRS + Event Sourcing over CRUD
- [ADR-002](adr/ADR-002-choreographed-saga-over-orchestrated.md): Choreographed SAGA
- [ADR-003](adr/ADR-003-virtual-threads-over-reactive.md): Virtual Threads over Reactive
