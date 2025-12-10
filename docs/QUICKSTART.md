# Quick Start Guide

Get the POC Microservices up and running in 5 minutes.

## Prerequisites

- Java 21
- Docker & Docker Compose
- Gradle 8.11.1+ (or use wrapper)

## Step 1: Start Infrastructure (2 minutes)

Start MongoDB, Kafka, and Vault:

```bash
docker-compose up -d
```

Verify containers are running:

```bash
docker-compose ps
```

You should see:
- `mongodb-poc` - MongoDB database
- `kafka-poc` - Kafka event streaming
- `vault-poc` - HashiCorp Vault

## Step 2: Configure Secrets (30 seconds)

Set up Vault with required secrets:

```bash
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='myroot'

# Products service secrets
vault kv put secret/products \
  mongodb.username=admin \
  mongodb.password=password \
  kafka.bootstrap-servers=localhost:9092

# Billing service secrets
vault kv put secret/billing \
  kafka.bootstrap-servers=localhost:9092
```

Verify secrets:

```bash
vault kv get secret/products
vault kv get secret/billing
```

## Step 3: Build Services (1 minute)

Build all microservices:

```bash
./gradlew clean build -x test
```

## Step 4: Start Services (1 minute)

Open 4 terminal windows and start services in order:

**Terminal 1 - Eureka Server:**
```bash
./gradlew :eureka-server:bootRun
```
Wait for: `Started EurekaServerApplication`

**Terminal 2 - API Gateway:**
```bash
./gradlew :api-gateway:bootRun
```
Wait for: `Started ApiGatewayApplication`

**Terminal 3 - Products Service:**
```bash
./gradlew :products:bootRun
```
Wait for: `Started ProductsApplication`

**Terminal 4 - Billing Service:**
```bash
./gradlew :billing:bootRun
```
Wait for: `Started BillingApplication`

## Step 5: Verify Everything Works (30 seconds)

### Check Eureka Dashboard

Open: http://localhost:8761

You should see:
- API-GATEWAY (1 instance)
- PRODUCTS (1+ instances)
- BILLING (1 instance)

### Test Products API

```bash
# Get all products
curl http://localhost:8080/products/products

# Create a product
curl -X POST http://localhost:8080/products/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Quick Start Product","price":99.99}'

# Verify it was created
curl http://localhost:8080/products/products
```

### Test Billing API

```bash
# Get all invoices
curl http://localhost:8080/billing/invoices
```

### View Swagger UI

Open: http://localhost:8080/products/swagger-ui/index.html

## What Just Happened?

1. **Infrastructure Started**: MongoDB, Kafka, and Vault containers running
2. **Secrets Configured**: Credentials stored securely in Vault
3. **Services Built**: All microservices compiled and packaged
4. **Services Started**: 
   - Eureka: Service registry
   - Gateway: API routing
   - Products: Event sourcing with MongoDB
   - Billing: Event consumer
5. **Verified**: APIs working through gateway

## Next Steps

### Explore Event Sourcing

View events in MongoDB:

```bash
docker exec -it mongodb-poc mongosh -u viewer -p viewonly123 \
  --authenticationDatabase products products \
  --eval "db.event_store.find().pretty()"
```

### Scale Products Service

Open a new terminal and start another instance:

```bash
./gradlew :products:bootRun
```

Check Eureka - you'll see 2 PRODUCTS instances!

### Monitor with Actuator

```bash
# Products health
curl http://localhost:8080/products/actuator/health

# Gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

### View Kafka Events

```bash
# List topics
docker exec -it kafka-poc kafka-topics --list --bootstrap-server localhost:9092

# Consume product events
docker exec -it kafka-poc kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic product-events \
  --from-beginning
```

## Troubleshooting

### Services won't start

**Issue**: Port already in use
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9
```

**Issue**: Vault connection failed
```bash
# Verify Vault is running
docker ps | grep vault
curl http://localhost:8200/v1/sys/health
```

### Gateway returns 500 errors

**Solution**: Restart gateway to refresh service registry
```bash
pkill -f "api-gateway:bootRun"
./gradlew :api-gateway:bootRun
```

### MongoDB connection refused

**Solution**: Check credentials in Vault
```bash
vault kv get secret/products
```

## Clean Up

Stop all services:

```bash
# Stop microservices (Ctrl+C in each terminal)

# Stop infrastructure
docker-compose down

# Remove volumes (optional - deletes all data)
docker-compose down -v
```

## Architecture Overview

```
Request Flow:
Client → API Gateway (8080) → Products Service (random port) → MongoDB
                                      ↓
                                    Kafka
                                      ↓
                              Billing Service (random port)
```

## Key URLs

| Service | URL |
|---------|-----|
| Eureka Dashboard | http://localhost:8761 |
| API Gateway | http://localhost:8080 |
| Products API | http://localhost:8080/products/products |
| Products Swagger | http://localhost:8080/products/swagger-ui/index.html |
| Billing API | http://localhost:8080/billing/invoices |
| Vault UI | http://localhost:8200 (Token: myroot) |

## What's Implemented

✅ Event Sourcing - Complete event history in MongoDB  
✅ CQRS - Separate read/write models  
✅ Command Bus - Command routing pattern  
✅ SAGA Pattern - Distributed transactions via Kafka  
✅ Service Discovery - Eureka registration  
✅ API Gateway - Centralized routing  
✅ Secret Management - Vault integration  
✅ Horizontal Scaling - Multiple instances  

## Learn More

- [Architecture Documentation](ARCHITECTURE.md) - Detailed architecture guide
- [README](README.md) - Complete project documentation
- [Docker Setup](DOCKER.md) - Infrastructure details

---

**You're now running a complete microservices architecture with Event Sourcing!** 🎉
