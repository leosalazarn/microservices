# Bean Validation Implementation

**Date**: 2025-12-10  
**Status**: ✅ Completed

## Overview

Added Jakarta Bean Validation to the microservices project using OpenAPI specification-first approach. Validation constraints are defined in OpenAPI specs and automatically generated into Java code.

## Changes Made

### 1. Products API Validation (`products/src/main/resources/openapi/products-api.yaml`)

```yaml
components:
  schemas:
    Product:
      type: object
      required:
        - name
        - price
      properties:
        id:
          type: integer
          format: int64
          readOnly: true
        name:
          type: string
          minLength: 1
          maxLength: 100
        price:
          type: number
          format: double
          minimum: 0.01
          exclusiveMinimum: false
        description:
          type: string
          maxLength: 500
        category:
          type: string
          maxLength: 50
```

**Generated Annotations:**
- `name`: `@NotNull @Size(min=1, max=100)`
- `price`: `@NotNull @DecimalMin("0.01")`
- `description`: `@Size(max=500)`
- `category`: `@Size(max=50)`
- `id`: Read-only (no validation)

### 2. Billing API Validation (`billing/src/main/resources/openapi/billing-api.yaml`)

```yaml
components:
  schemas:
    Invoice:
      type: object
      required:
        - customerId
        - amount
        - status
      properties:
        id:
          type: integer
          format: int64
          readOnly: true
        customerId:
          type: integer
          format: int64
          minimum: 1
        amount:
          type: number
          format: double
          minimum: 0.01
          exclusiveMinimum: false
        status:
          type: string
          enum: [PENDING, PAID, CANCELLED]
```

**Generated Annotations:**
- `customerId`: `@NotNull @Min(1)`
- `amount`: `@NotNull @DecimalMin("0.01")`
- `status`: `@NotNull` (enum validation)
- `id`: Read-only (no validation)

### 3. OpenAPI Generator Configuration

**Products Service** (`products/build.gradle`):
```gradle
openApiGenerate {
    generatorName = "spring"
    inputSpec = "$projectDir/src/main/resources/openapi/products-api.yaml"
    outputDir = "$buildDir/generated"
    apiPackage = "com.example.products.api"
    modelPackage = "com.example.products.model"
    configOptions = [
        interfaceOnly: "true",
        useTags: "true",
        useJakartaEe: "true",
        useBeanValidation: "true"  // ← Added
    ]
}
```

**Billing Service** (`billing/build.gradle`):
```gradle
openApiGenerate {
    generatorName = "spring"
    inputSpec = "$projectDir/src/main/resources/openapi/billing-api.yaml"
    outputDir = "$buildDir/generated"
    apiPackage = "com.example.billing.api"
    modelPackage = "com.example.billing.model"
    configOptions = [
        interfaceOnly: "true",
        useTags: "true",
        useJakartaEe: "true",
        useBeanValidation: "true"  // ← Added
    ]
}
```

## Dependencies

Both services already had the required dependency:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## Generated Code Examples

### Product Model
```java
public class Product {
    @NotNull @Size(min = 1, max = 100)
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @NotNull @DecimalMin("0.01")
    @JsonProperty("price")
    public Double getPrice() {
        return price;
    }

    @Size(max = 500)
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }
}
```

### API Interface
```java
@PostMapping(
    value = "/products",
    produces = { "application/json" },
    consumes = { "application/json" }
)
default ResponseEntity<Product> createProduct(
    @Parameter(name = "Product", description = "", required = true) 
    @Valid @RequestBody Product product  // ← @Valid annotation
) {
    // ...
}
```

## How It Works

1. **OpenAPI Spec** defines validation constraints (required, minLength, minimum, etc.)
2. **OpenAPI Generator** reads specs and generates Java models with Jakarta Bean Validation annotations
3. **Spring Boot** automatically validates `@Valid` request bodies before controller methods execute
4. **Invalid requests** return HTTP 400 with detailed validation error messages

## Testing Validation

### Valid Request
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Premium Widget","price":149.99}'
```

### Invalid Requests

**Missing required fields:**
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{}'
# Returns 400: name and price are required
```

**Price too low:**
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":0}'
# Returns 400: price must be at least 0.01
```

**Name too long:**
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"'$(printf 'A%.0s' {1..101})'","price":10}'
# Returns 400: name must be at most 100 characters
```

**Invalid customerId:**
```bash
curl -X POST http://localhost:8080/billing/invoices \
  -H "Content-Type: application/json" \
  -d '{"customerId":0,"amount":100,"status":"PENDING"}'
# Returns 400: customerId must be at least 1
```

## Build Commands

```bash
# Regenerate OpenAPI code
./gradlew :products:clean :products:openApiGenerate
./gradlew :billing:clean :billing:openApiGenerate

# Build services
./gradlew :products:build :billing:build -x test

# Full clean build
./gradlew clean build -x test
```

## Validation Rules Summary

### Products Service
| Field | Required | Constraints |
|-------|----------|-------------|
| name | ✅ Yes | 1-100 characters |
| price | ✅ Yes | ≥ 0.01 |
| description | ❌ No | ≤ 500 characters |
| category | ❌ No | ≤ 50 characters |
| id | N/A | Read-only |

### Billing Service
| Field | Required | Constraints |
|-------|----------|-------------|
| customerId | ✅ Yes | ≥ 1 |
| amount | ✅ Yes | ≥ 0.01 |
| status | ✅ Yes | PENDING, PAID, or CANCELLED |
| id | N/A | Read-only |

## Benefits

✅ **Contract-First**: Validation rules defined in OpenAPI specs  
✅ **Type-Safe**: Generated code with compile-time checks  
✅ **Automatic**: Spring Boot validates before controller execution  
✅ **Consistent**: Same validation rules in API docs and code  
✅ **Maintainable**: Single source of truth for validation rules  
✅ **Self-Documenting**: Swagger UI shows validation constraints  

## Validation Strategy for CQRS + MongoDB

### ✅ Where to Apply Validation

| Layer | Validation Type | Purpose | Implementation |
|-------|----------------|---------|----------------|
| **API DTOs** | Bean Validation | Input validation at API boundary | `@Valid` on DTOs (OpenAPI generated) |
| **Commands** | Bean Validation | Validate command intent | `@NotNull`, `@Size` on Command classes |
| **Aggregates** | Business Rules | Domain logic validation | Manual validation in methods |
| **Entities** | ❌ NO Validation | Read models only | MongoDB @Document (no validation) |

### Why NOT Validate Entities in CQRS?

**MongoDB Entities are Read Models (Query Side):**
- Entities represent **persisted state**, not input
- Validation on entities would validate **stale data** on reads
- Breaks CQRS separation: Commands write, Queries read
- Event Sourcing: Entities are **projections** of events

**Validation Belongs on Command Side:**
- Commands represent **user intent** (write operations)
- Validate **before** state changes
- Aggregates enforce **business rules**
- Entities just store the **result**

### Command Validation Implementation

**CreateProductCommand** with Bean Validation:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductCommand implements Command {
    
    @NotNull(message = "Product name is required")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    private String name;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be at least 0.01")
    private Double price;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;
}
```

**CommandBus** with Automatic Validation:
```java
@Component
@RequiredArgsConstructor
public class CommandBus {
    
    private final Validator validator;
    private final Map<Class<? extends Command>, CommandHandler<? extends Command, ?>> handlers = new HashMap<>();
    
    public <C extends Command, R> R dispatch(C command) {
        // Validate command before dispatching
        Set<ConstraintViolation<C>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Command validation failed: " + errors);
        }
        
        CommandHandler<C, R> handler = (CommandHandler<C, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for command: " + command.getClass().getName());
        }
        return handler.handle(command);
    }
}
```

### Validation Flow

```
┌─────────────┐
│  API Layer  │  ← @Valid on DTOs (OpenAPI generated)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Controller │  ← Maps DTO to Command
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Command Bus │  ← Validates Command with Jakarta Validator
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Handler   │  ← Executes business logic
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Aggregate  │  ← Business rule validation (manual)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Entity    │  ← NO validation (just persistence)
└─────────────┘
```

### Entity Example (NO Validation)

**ProductEntity** - MongoDB Document:
```java
@Data
@NoArgsConstructor
@Document(collection = "products")
public class ProductEntity {
    
    @Id
    private String id;
    
    @Field("name")
    @Indexed
    private String name;  // ← NO @NotNull, NO @Size
    
    @Field("price")
    private Double price;  // ← NO @DecimalMin
    
    @Field("description")
    private String description;
    
    @Field("category")
    @Indexed
    private String category;
    
    @Field("active")
    private Boolean active = true;
    
    // ... timestamps, version, etc.
}
```

### Aggregate Example (Business Rules)

**ProductAggregate** - Domain Logic:
```java
public class ProductAggregate {
    
    // Business method with validation
    public void updatePrice(Double newPrice) {
        validatePrice(newPrice);  // ← Manual business rule validation
        
        if (this.price.equals(newPrice)) {
            throw new IllegalArgumentException("New price must be different from current price");
        }
        
        this.price = newPrice;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    private static void validatePrice(Double price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (price > 1000000) {
            throw new IllegalArgumentException("Product price cannot exceed 1,000,000");
        }
    }
}
```

## Summary: 3-Layer Validation

1. **API Layer** (DTOs): Input format validation
2. **Command Layer**: Command intent validation
3. **Aggregate Layer**: Business rule validation
4. **Entity Layer**: ❌ NO validation (read models)

## Next Steps (Optional)

- [ ] Add custom validation messages in OpenAPI specs
- [ ] Implement custom validators for complex business rules
- [ ] Add validation error response DTOs
- [ ] Configure validation groups for different scenarios
- [ ] Add integration tests for validation scenarios
- [ ] Add validation to other commands (UpdateProduct, DeleteProduct, etc.)

## References

- [Jakarta Bean Validation](https://beanvalidation.org/)
- [OpenAPI Validation Keywords](https://swagger.io/docs/specification/data-models/data-types/)
- [Spring Boot Validation](https://spring.io/guides/gs/validating-form-input/)
- [OpenAPI Generator Spring](https://openapi-generator.tech/docs/generators/spring/)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
