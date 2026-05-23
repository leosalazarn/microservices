# Quick Start Guide

Get the POC Microservices up and running in 5 minutes.

## Prerequisites

- Java 21, Docker & Docker Compose, Gradle 8.11.1+ (or use wrapper)

## Step 1: Start Infrastructure

```bash
docker-compose up -d
```

## Step 2: Configure Vault Secrets

```bash
export VAULT_ADDR='http://localhost:8200' VAULT_TOKEN='myroot'
vault kv put secret/products mongodb.username=admin mongodb.password=password kafka.bootstrap-servers=localhost:9092
vault kv put secret/billing kafka.bootstrap-servers=localhost:9092
```

## Step 3: Build & Start (4 terminals)

```bash
# Terminal 1 — Eureka
./gradlew :eureka-server:bootRun
# Terminal 2 — Gateway
./gradlew :api-gateway:bootRun
# Terminal 3 — Products
./gradlew :products:bootRun
# Terminal 4 — Billing
./gradlew :billing:bootRun
```

## Step 4: Verify

```bash
curl http://localhost:8080/products/products
curl -X POST http://localhost:8080/products/products -H "Content-Type: application/json" -d '{"name":"Test","price":99.99}'
curl http://localhost:8080/billing/invoices
```

## Key URLs

| Service          | URL                                                  |
|------------------|------------------------------------------------------|
| Eureka Dashboard | http://localhost:8761                                |
| API Gateway      | http://localhost:8080                                |
| Swagger UI       | http://localhost:8080/products/swagger-ui/index.html |
| Vault UI         | http://localhost:8200 (Token: myroot)                |

## Clean Up

```bash
docker-compose down -v
```
