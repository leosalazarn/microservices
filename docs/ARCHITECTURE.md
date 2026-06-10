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
    KAFKA["Kafka :19092"]
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
    Listener->>Redis: CacheManager.clear("products")
    Note over Redis: products::all cleared
    Aggregate-->>Bus: result
    Bus-->>CmdCtrl: result
    CmdCtrl-->>Client: response

    Note over Client,Redis: Next GET rebuilds cache
```

> **Note**: Cache eviction uses direct `CacheManager` calls instead of `@CacheEvict` annotation
> because Spring calls `@EventListener` methods directly (bypassing AOP proxies).
> For multi-instance production, use Kafka/Redis Pub/Sub for cross-instance eviction.

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
- Idempotent producer (`acks=all`, `retries=10`) + gzip compression for reliable delivery

### Event-Driven Cache Invalidation

```
Write → DomainEvent → ApplicationEventPublisher → @EventListener → CacheManager.clear("products") → Redis cleared
```

> **Note**: In-process `@EventListener` works for single-instance POC. Multi-instance production needs Kafka/Redis
> Pub/Sub for cross-instance eviction. See `CacheInvalidationEventHandler.java` for the `CacheManager`-based approach.

### Distributed Tracing

```
HTTP request → Gateway → Products/Billing → Zipkin collector (Micrometer Tracing + Brave)
```

Every service emits traces with `traceId` and `spanId`. All traces are collected by Zipkin at `http://localhost:9411`.
Spring Cloud Gateway and Spring Kafka auto-instrument — no custom Observation handlers needed.

---

## Tech Stack

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| Framework          | Spring Boot 3.4.5 / Spring Cloud 2024.0.1       |
| Language           | Java 21 (Virtual Threads — ADR-003)             |
| Service Discovery  | Netflix Eureka                                  |
| API Gateway        | Spring Cloud Gateway                            |
| Database           | MongoDB 8.0                                     |
| Event Streaming    | Apache Kafka 3.9.2 (gzip compression)           |
| Cache              | Redis 7                                         |
| Secrets            | HashiCorp Vault                                 |
| Tracing            | Zipkin · Micrometer Tracing Bridge Brave        |
| Kafka Tools        | Kafdrop · Kafka UI                              |
| API Docs           | OpenAPI 3.0.3 / Swagger UI / SpringDoc          |

## ADRs

See `docs/adr/` for Architecture Decision Records:

- [ADR-001](adr/ADR-001-cqrs-event-sourcing-over-crud.md): CQRS + Event Sourcing over CRUD
- [ADR-002](adr/ADR-002-choreographed-saga-over-orchestrated.md): Choreographed SAGA
- [ADR-003](adr/ADR-003-virtual-threads-over-reactive.md): Virtual Threads over Reactive
