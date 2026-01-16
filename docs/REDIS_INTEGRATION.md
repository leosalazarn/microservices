# Redis Integration Example

This document demonstrates how Redis caching is integrated into the Products microservice.

## Architecture Integration

Redis is used as a distributed cache layer to optimize query performance in the CQRS pattern:

- **Query Side**: Results are cached with TTL (Time To Live)
- **Command Side**: Cache is invalidated when data changes
- **Pattern**: Cache-Aside (Lazy Loading)

## Implementation

### 1. Query Caching

The `ProductQueryHandler` uses Spring's `@Cacheable` annotation:

```java
@Cacheable(value = "products", key = "'all'")
public List<Product> getAllProducts() {
    log.info("Fetching products from database (cache miss)");
    return repository.findByActiveTrue()
            .stream()
            .map(mapper::toModel)
            .collect(Collectors.toList());
}
```

**Behavior:**
- First call: Queries MongoDB and stores result in Redis
- Subsequent calls: Returns cached data (no DB query)
- Cache TTL: 10 minutes (configurable in `RedisConfig`)

### 2. Cache Invalidation

The `CreateProductCommandHandler` uses `@CacheEvict` annotation:

```java
@CacheEvict(value = "products", key = "'all'")
public Product handle(CreateProductCommand command) {
    // Create product logic
}
```

**Behavior:**
- When a product is created, the cache is cleared
- Next GET request will rebuild the cache with fresh data

## Testing the Cache

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
  kafka.bootstrap-servers=localhost:9092 \
  redis.host=localhost \
  redis.port=6379 \
  redis.password=redispassword
```

### 3. Start Products Service

```bash
./gradlew :products:bootRun
```

### 4. Test Cache Behavior

**First Request (Cache Miss):**
```bash
curl http://localhost:8080/products/products
```

Check logs - you'll see: `Fetching products from database (cache miss)`

**Verify Cache in Redis:**
```bash
docker exec -it redis-poc redis-cli -a redispassword GET "products::all"
```

**Second Request (Cache Hit):**
```bash
curl http://localhost:8080/products/products
```

No database query log - data served from cache!

**Create New Product (Cache Eviction):**
```bash
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Product","price":99.99}'
```

Cache is now cleared.

**Next GET Request:**
```bash
curl http://localhost:8080/products/products
```

Cache miss again - rebuilds cache with new product included.

## Redis Operations

### Monitor Cache Activity

```bash
# Watch all Redis commands in real-time
docker exec -it redis-poc redis-cli -a redispassword MONITOR
```

### View Cache Keys

```bash
docker exec -it redis-poc redis-cli -a redispassword KEYS "*"
```

### Check Cache TTL

```bash
docker exec -it redis-poc redis-cli -a redispassword TTL "products::all"
```

Returns remaining seconds until expiration.

### Clear All Cache

```bash
docker exec -it redis-poc redis-cli -a redispassword FLUSHALL
```

### Get Cache Statistics

```bash
docker exec -it redis-poc redis-cli -a redispassword INFO stats
```

## Configuration

### Cache TTL

Modify TTL in `RedisConfig.java`:

```java
RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
    .entryTtl(Duration.ofMinutes(10));  // Change this value
```

### Cache Names

Add more caches in `@Cacheable` annotations:

```java
@Cacheable(value = "products-by-id", key = "#id")
public Product getProductById(Long id) {
    // Implementation
}
```

## Performance Benefits

### Without Cache
- Every GET request queries MongoDB
- Response time: ~50-100ms
- Database load: High

### With Cache
- First request: ~50-100ms (cache miss)
- Subsequent requests: ~5-10ms (cache hit)
- Database load: Minimal
- **Performance improvement: 10-20x faster**

## Use Cases

1. **Product Catalog**: Cache frequently accessed product lists
2. **Session Storage**: Store user sessions across service instances
3. **Rate Limiting**: Track API request counts per user
4. **Distributed Locks**: Coordinate operations across instances
5. **Real-time Analytics**: Cache aggregated metrics

## Advanced Usage

The `RedisCacheService` provides additional operations:

```java
// Store with custom TTL
redisCacheService.set("key", value, Duration.ofHours(1));

// Increment counter
redisCacheService.increment("api:requests:user123");

// Store complex objects as hash
redisCacheService.setHash("user:123", "email", "user@example.com");
```

## Troubleshooting

### Connection Issues

Check Redis is running:
```bash
docker ps | grep redis-poc
```

Test connection:
```bash
docker exec -it redis-poc redis-cli -a redispassword PING
```

Should return: `PONG`

### Cache Not Working

1. Check logs for Redis connection errors
2. Verify Vault configuration
3. Ensure `@EnableCaching` is present in config
4. Check cache key matches in `@Cacheable` and `@CacheEvict`

### Memory Issues

Monitor Redis memory:
```bash
docker exec -it redis-poc redis-cli -a redispassword INFO memory
```

Set max memory in docker-compose.yml:
```yaml
command: redis-server --requirepass redispassword --maxmemory 256mb --maxmemory-policy allkeys-lru
```

## Best Practices

1. **Use appropriate TTL**: Balance freshness vs performance
2. **Cache invalidation**: Always evict cache on data changes
3. **Key naming**: Use consistent, descriptive cache keys
4. **Monitor memory**: Set maxmemory and eviction policies
5. **Serialization**: Use JSON for complex objects
6. **Error handling**: Gracefully handle cache failures (fallback to DB)

## References

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
