# Redis Integration

Redis is used as a distributed cache layer in the CQRS pattern:

- **Query Side**: Results cached via `@Cacheable` with TTL
- **Command Side**: Cache invalidated on change via `@CacheEvict`
- **Pattern**: Cache-Aside (Lazy Loading)

## Cache Flow

1. First GET request → cache miss → query MongoDB → store in Redis
2. Subsequent GET requests → cache hit → serve from Redis (no DB query)
3. POST/PUT (write) → `@CacheEvict` clears cache → next GET rebuilds from DB

## Cache Invalidation

Invalidation is event-driven via `@EventListener`:

- `ProductCreatedEvent` → evicts `products::all` cache
- `ProductUpdatedEvent` → evicts `products::all` cache
- `POST /products/cache/evict` → manual eviction of all caches

> **Note for production (multi-instance):** `@EventListener` is in-process
> (`ApplicationEventPublisher`), so only the instance handling the write evicts its
> local cache. For multi-instance deployments, evict via the same Kafka event or
> Redis Pub/Sub instead.
