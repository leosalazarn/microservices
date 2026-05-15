# ADR-002: Choreographed SAGA over Orchestrated SAGA

**Status**: Accepted  
**Date**: 2026-05-15  
**Deciders**: Tech Lead  
**Driver**: Need for distributed transaction coordination across Products and Billing services without a central orchestrator

## Context

When a Product is created, the Billing service must create a corresponding billing record. This spans two services — no single database transaction can guarantee consistency. Options for distributed coordination:

1. **Orchestrated SAGA**: A dedicated orchestrator service sends sequential commands and handles compensation
2. **Choreographed SAGA**: Each service produces/consumes events independently, forming a coordination chain

## Decision

Use **Choreographed SAGA** with **Kafka as the event backbone**:

```
ProductCreated → Kafka [product-events] → BillingConsumer → creates Invoice
        ↑                                              │
        └──── InvoiceCreated → Kafka [billing-events] ─┘
```

### Key implementation choices

- **Kafka topics** as the choreography channel — one topic per domain (`product-events`, `billing-events`)
- **Event-driven consumers** — `ProductCreatedEventHandler` in billing reacts to product events
- **No dedicated orchestrator** — the event flow itself defines the SAGA

## Consequences

### Positive
- No single point of failure — no orchestrator to go down
- Services remain loosely coupled — only share Kafka topic contracts (OpenAPI-style event schemas)
- Scales horizontally — each service scales independently

### Negative
- Eventual consistency — billing may lag behind product creation (acceptable: < 1s window)
- Harder to trace failures — compensation logic is distributed (mitigated by Kafka header tracing)
- No centralized rollback — each service must handle its own compensation

## Alternatives Considered

### Orchestrated SAGA with a dedicated SAGA Coordinator
- **Rejected**: Introduces a new service and single point of failure
- Benefits (centralized monitoring) don't justify the operational complexity for a 2-service POC

### Two-Phase Commit (2PC)
- **Rejected**: Blocking protocol, not suitable for microservices
- Would couple both services to a distributed transaction coordinator

## Related ADRs
- ADR-001: CQRS + Event Sourcing over Traditional CRUD
