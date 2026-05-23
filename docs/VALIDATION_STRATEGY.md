# Validation Strategy for CQRS + Event Sourcing + MongoDB

## TL;DR

✅ **Validate**: API DTOs, Commands, Aggregates (business rules)  
❌ **Don't Validate**: MongoDB Entities (read models)

## 3-Layer Validation Architecture

### Layer 1: API DTOs (Input Validation)
**Purpose**: Validate HTTP request format  
**Technology**: Jakarta Bean Validation (OpenAPI-generated, `@Valid`)  
**What**: Data types, required fields, format constraints

### Layer 2: Commands (Intent Validation)
**Purpose**: Validate command intent before execution  
**Technology**: Jakarta Bean Validation + `CommandBus`  
**What**: Command structure, basic constraints — validated automatically before dispatch

### Layer 3: Aggregates (Business Rules)
**Purpose**: Enforce domain business rules  
**Technology**: Manual validation in domain methods  
**What**: State transitions, business invariants, domain logic

### Layer 4: Entities (NO Validation)
**Purpose**: Store data (read models, projections from events)  
**What**: Nothing — entities are persisted event projections, not validated

## Why NOT Entities

- **CQRS**: Entities are read models, validation belongs on the write side (commands/aggregates)
- **Event Sourcing**: Events are immutable facts; entities are projections rebuilt from them
- **MongoDB**: NoSQL is schema-less — enforce schema at application boundaries, not in storage

## Flow

```
HTTP Request → API Validation (@Valid) → Controller → CommandBus Validation
  → Handler → Aggregate Business Rules → Events → Entity (no validation) → Response
```
