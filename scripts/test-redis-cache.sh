#!/bin/bash

# Redis Cache Testing Script
# This script demonstrates Redis caching behavior in the Products service

set -e

GATEWAY_URL="http://localhost:8080/products"
REDIS_CONTAINER="redis-poc"
REDIS_PASSWORD="redispassword"

echo "=========================================="
echo "Redis Cache Testing Script"
echo "=========================================="
echo ""

# Function to execute Redis commands
redis_cmd() {
    docker exec -it $REDIS_CONTAINER redis-cli -a $REDIS_PASSWORD --no-auth-warning "$@"
}

# Function to make API request and measure time
api_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    echo "Making $method request to $endpoint..."
    if [ "$method" = "GET" ]; then
        time curl -s "$GATEWAY_URL$endpoint" | jq '.'
    else
        time curl -s -X POST "$GATEWAY_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" | jq '.'
    fi
}

echo "Step 1: Clear existing cache"
echo "----------------------------"
redis_cmd FLUSHALL
echo "✓ Cache cleared"
echo ""

echo "Step 2: First GET request (Cache Miss)"
echo "---------------------------------------"
echo "This will query MongoDB and populate the cache"
api_request GET "/products"
echo ""

echo "Step 3: Check Redis cache"
echo "-------------------------"
echo "Cache key 'products::all' should now exist:"
redis_cmd EXISTS "products::all"
echo ""
echo "Cache TTL (seconds remaining):"
redis_cmd TTL "products::all"
echo ""

echo "Step 4: Second GET request (Cache Hit)"
echo "--------------------------------------"
echo "This should be much faster - served from Redis"
api_request GET "/products"
echo ""

echo "Step 5: Create new product (Cache Eviction)"
echo "-------------------------------------------"
api_request POST "/products" '{"name":"Redis Test Product","price":199.99}'
echo ""

echo "Step 6: Verify cache was evicted"
echo "--------------------------------"
echo "Cache key should not exist anymore:"
redis_cmd EXISTS "products::all"
echo ""

echo "Step 7: Third GET request (Cache Miss again)"
echo "--------------------------------------------"
echo "Cache was evicted, so this queries MongoDB again"
api_request GET "/products"
echo ""

echo "Step 8: View all cache keys"
echo "---------------------------"
redis_cmd KEYS "*"
echo ""

echo "Step 9: Cache statistics"
echo "-----------------------"
redis_cmd INFO stats | grep -E "keyspace_hits|keyspace_misses"
echo ""

echo "=========================================="
echo "Testing Complete!"
echo "=========================================="
echo ""
echo "To monitor Redis in real-time, run:"
echo "  docker exec -it redis-poc redis-cli -a redispassword MONITOR"
echo ""
