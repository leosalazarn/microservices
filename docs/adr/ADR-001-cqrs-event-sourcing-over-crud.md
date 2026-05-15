# ADR-001: CQRS + Event Sourcing over Traditional CRUD

**Status**: Accepted  
**Date**: 2026-05-15  
**Deciders**: Tech Lead (Architectural Decision)  
**Driver**: Need for audit trail, temporal queries, and decoupled read/write paths in a multi-microservice domain

## Context

The Products service needs to handle product mutations (create, update, delete) while supporting:
- Full audit history of all changes
- Ability to reconstruct state at any point in time
- Publishing events to Kafka for downstream consumers (Billing, future AI services)
- Transactional consistency across aggregates

A traditional CRUD approach (direct `save()` to MongoDB) was the simpler alternative, but fails on audit and event-driven requirements without additional ceremony.

## Decision

Use **CQRS (Command Query Responsibility Segregation)** with **Event Sourcing**:

```
Command Path: Controller → Command → CommandHandler → Aggregate → EventStore → Kafka
Query Path:   Controller → Query → QueryHandler → MongoDB (read model)
```

### Key implementation choices

- **Command Bus**: In-memory dispatcher with middleware for validation + logging
- **Event Store**: MongoDB-backed append-only log (`EventStoreEntity` collection)
- **Read Model**: MongoDB document (`ProductEntity`) updated reactively after events
- **Kafka Publishing**: `EventPublisher` publishes domain events to `product-events` topic

## Consequences

### Positive
- Complete audit trail — every state change is recorded permanently
- Temporal queries — can rebuild a product's state at any past version
- Event-driven integration — Kafka topics are the integration contract for downstream services
- Clear separation — read and write concerns evolve independently

### Negative
- Higher complexity — 2x the classes versus naive CRUD
- Event versioning — renaming event classes breaks deserialization of stored events (mitigated by storing FQCN, see Phase 3)
- Read model eventual consistency — read model may lag behind write model (acceptable for this domain)

## Alternatives Considered

### Plain CRUD with `@Version` optimistic locking
- **Rejected**: No audit trail, no event stream for Kafka, no temporal queries
- Would require adding an explicit `AuditEntry` entity + event publishing layer, reimplementing Event Sourcing poorly

### CRUD with MongoDB Change Streams
- **Rejected**: Ties event consumption to MongoDB infrastructure; harder to test; no explicit domain events
- Change Streams push database-level diffs (not domain events), coupling consumers to DB schema

## Related ADRs
- ADR-002: Choreographed SAGA over Orchestrated
- ADR-003: Virtual Threads over Reactive Stack
