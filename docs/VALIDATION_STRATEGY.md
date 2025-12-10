# Validation Strategy for CQRS + Event Sourcing + MongoDB

**Date**: 2025-12-10  
**Architecture**: CQRS, Event Sourcing, MongoDB (NoSQL)

## TL;DR

✅ **Validate**: API DTOs, Commands, Aggregates (business rules)  
❌ **Don't Validate**: MongoDB Entities (read models)

## Why NOT Hibernate Validator on MongoDB Entities?

### 1. CQRS Separation of Concerns

```
Command Side (Write)          Query Side (Read)
┌──────────────────┐         ┌──────────────────┐
│   Commands       │         │   Queries        │
│   ↓              │         │   ↓              │
│   Aggregates     │  ──→    │   Entities       │
│   ↓              │ Events  │   (Read Models)  │
│   Event Store    │         │                  │
└──────────────────┘         └──────────────────┘
    VALIDATE HERE              DON'T VALIDATE
```

### 2. Entities are Projections

- **Entities** = Result of applied events (read models)
- **Commands** = User intent (write operations)
- Validation happens **before** events are created
- Entities just **store** the validated result

### 3. Event Sourcing Implications

- Events are **immutable facts** that already happened
- Entities are **projections** rebuilt from events
- Validating projections = validating history (wrong!)
- Validation must happen **before** event creation

### 4. MongoDB is Schema-less

- NoSQL databases don't enforce schema
- Validation at entity level conflicts with NoSQL flexibility
- Better to validate at **application boundaries** (API, Commands)

## 3-Layer Validation Architecture

### Layer 1: API DTOs (Input Validation)

**Purpose**: Validate HTTP request format  
**Technology**: Jakarta Bean Validation (OpenAPI generated)  
**Location**: Generated model classes

```java
// Generated from OpenAPI spec
public class Product {
    @NotNull @Size(min = 1, max = 100)
    private String name;
    
    @NotNull @DecimalMin("0.01")
    private Double price;
}

// Controller
@PostMapping("/products")
ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
    // Spring validates automatically
}
```

**Validates**: Data types, required fields, format constraints

### Layer 2: Commands (Intent Validation)

**Purpose**: Validate command intent before execution  
**Technology**: Jakarta Bean Validation + CommandBus  
**Location**: Command classes

```java
@Data
public class CreateProductCommand implements Command {
    @NotNull(message = "Product name is required")
    @Size(min = 1, max = 100, message = "Name must be 1-100 characters")
    private String name;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private Double price;
}

// CommandBus validates before dispatching
public <C extends Command, R> R dispatch(C command) {
    Set<ConstraintViolation<C>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
        throw new IllegalArgumentException("Command validation failed");
    }
    return handler.handle(command);
}
```

**Validates**: Command structure, basic constraints

### Layer 3: Aggregates (Business Rules)

**Purpose**: Enforce domain business rules  
**Technology**: Manual validation in domain methods  
**Location**: Aggregate classes

```java
public class ProductAggregate {
    
    public void updatePrice(Double newPrice) {
        // Business rule validation
        validatePrice(newPrice);
        
        if (this.price.equals(newPrice)) {
            throw new IllegalArgumentException("New price must differ from current");
        }
        
        if (!this.active) {
            throw new IllegalStateException("Cannot update inactive product");
        }
        
        this.price = newPrice;
        this.version++;
    }
    
    private void validatePrice(Double price) {
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (price > 1000000) {
            throw new IllegalArgumentException("Price exceeds maximum");
        }
    }
}
```

**Validates**: Business invariants, state transitions, domain rules

### Layer 4: Entities (NO Validation)

**Purpose**: Store data (read models)  
**Technology**: MongoDB @Document  
**Location**: Entity classes

```java
@Data
@Document(collection = "products")
public class ProductEntity {
    @Id
    private String id;
    
    @Field("name")
    private String name;  // ← NO @NotNull, NO @Size
    
    @Field("price")
    private Double price;  // ← NO @DecimalMin
    
    @Field("active")
    private Boolean active;
    
    // NO validation annotations!
}
```

**Validates**: Nothing - just persistence

## Complete Validation Flow

```
1. HTTP Request
   ↓
2. API DTO Validation (@Valid)
   ├─ Valid → Continue
   └─ Invalid → 400 Bad Request
   ↓
3. Controller maps DTO → Command
   ↓
4. CommandBus validates Command
   ├─ Valid → Dispatch to Handler
   └─ Invalid → IllegalArgumentException
   ↓
5. Handler creates/loads Aggregate
   ↓
6. Aggregate validates Business Rules
   ├─ Valid → Apply changes, raise events
   └─ Invalid → Domain exception
   ↓
7. Events persisted to Event Store
   ↓
8. Entity updated (projection)
   ↓
9. Response returned
```

## Implementation Checklist

### ✅ Completed

- [x] API DTOs with Bean Validation (OpenAPI generated)
- [x] OpenAPI generator configured with `useBeanValidation: true`
- [x] Commands with Bean Validation annotations
- [x] CommandBus with automatic validation
- [x] Aggregates with business rule validation
- [x] Entities without validation (read models)

### Dependencies

```gradle
// Already present in both services
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## Testing Validation

### Test API Layer
```bash
# Missing required field
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{}'
# Expected: 400 Bad Request - "name is required"

# Invalid price
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":0}'
# Expected: 400 Bad Request - "price must be at least 0.01"
```

### Test Command Layer
```java
@Test
void shouldValidateCommand() {
    CreateProductCommand command = new CreateProductCommand();
    command.setName("");  // Invalid
    command.setPrice(-1.0);  // Invalid
    
    assertThrows(IllegalArgumentException.class, 
        () -> commandBus.dispatch(command));
}
```

### Test Aggregate Layer
```java
@Test
void shouldEnforceBusinessRules() {
    ProductAggregate product = new ProductAggregate();
    product.setActive(false);
    
    assertThrows(IllegalStateException.class,
        () -> product.updatePrice(100.0));
}
```

## Common Mistakes to Avoid

❌ **Don't**: Add `@NotNull` to MongoDB entity fields  
✅ **Do**: Validate in Commands and Aggregates

❌ **Don't**: Use `@Valid` on entity save operations  
✅ **Do**: Validate before creating commands

❌ **Don't**: Mix validation concerns across layers  
✅ **Do**: Keep clear separation: API → Command → Aggregate → Entity

❌ **Don't**: Validate read operations (queries)  
✅ **Do**: Only validate write operations (commands)

## Benefits of This Approach

1. **Clear Separation**: Each layer has distinct validation responsibility
2. **CQRS Compliant**: Commands validated, queries not
3. **Event Sourcing Compatible**: Validation before event creation
4. **MongoDB Friendly**: No schema enforcement at entity level
5. **Testable**: Each validation layer can be tested independently
6. **Maintainable**: Validation rules in appropriate locations
7. **Performance**: No unnecessary validation on reads

## References

- [CQRS Pattern - Martin Fowler](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing - Martin Fowler](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Jakarta Bean Validation](https://beanvalidation.org/)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Aggregate Pattern](https://martinfowler.com/bliki/DDD_Aggregate.html)
