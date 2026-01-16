# Redis Quick Reference Card

## Start Redis
```bash
docker-compose up -d redis
```

## Configure Vault
```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='myroot'

vault kv put secret/products \
  redis.host=localhost \
  redis.port=6379 \
  redis.password=redispassword
```

## Test Cache
```bash
# Run automated test
./scripts/test-redis-cache.sh

# Manual test - First request (cache miss)
curl http://localhost:8080/products/products

# Second request (cache hit - faster!)
curl http://localhost:8080/products/products

# Create product (evicts cache)
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","price":99.99}'
```

## Monitor Cache
```bash
# Connect to Redis CLI
docker exec -it redis-poc redis-cli -a redispassword

# View all keys
KEYS *

# Get cache value
GET "products::all"

# Check TTL (seconds remaining)
TTL "products::all"

# Watch real-time operations
MONITOR

# Get statistics
INFO stats

# Clear all cache
FLUSHALL
```

## Common Operations
```bash
# Check if Redis is running
docker ps | grep redis-poc

# View Redis logs
docker logs redis-poc

# Restart Redis
docker-compose restart redis

# Stop Redis
docker-compose stop redis

# Remove Redis (including data)
docker-compose down -v redis
```

## Cache Behavior
- **GET /products** (first time): MongoDB query → Store in Redis (10 min TTL)
- **GET /products** (cached): Instant response from Redis
- **POST /products**: Cache evicted → Next GET rebuilds cache

## Performance
- **Without cache**: ~50-100ms per request
- **With cache**: ~5-10ms per request
- **Improvement**: 10-20x faster

## Troubleshooting
```bash
# Test Redis connection
docker exec -it redis-poc redis-cli -a redispassword PING
# Should return: PONG

# Check Redis memory usage
docker exec -it redis-poc redis-cli -a redispassword INFO memory

# View connected clients
docker exec -it redis-poc redis-cli -a redispassword CLIENT LIST
```

## Configuration
- **Host**: localhost
- **Port**: 6379
- **Password**: redispassword
- **TTL**: 10 minutes
- **Cache Key**: products::all

## Documentation
- Full guide: `docs/REDIS_INTEGRATION.md`
- Summary: `docs/REDIS_SUMMARY.md`
- Main README: `README.md` (Redis sections)
