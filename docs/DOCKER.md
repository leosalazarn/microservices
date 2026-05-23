# Docker Setup

## Infrastructure Dependencies

All infrastructure runs via Docker Compose.

### Quick Start

```bash
docker-compose up -d
```

This starts all infrastructure (MongoDB, Kafka, Vault, Redis) and all 4 microservices (Eureka, Products, Billing, API
Gateway).

### Services

| Service     | Port  | Notes                        |
|-------------|-------|------------------------------|
| API Gateway | 8080  | Entry point for all requests |
| Products    | 8081  | Product management + CQRS    |
| Billing     | 8082  | Invoice management + SAGA    |
| Eureka      | 8761  | Service discovery            |
| MongoDB     | 27017 | Document database            |
| Kafka       | 9092  | Event streaming              |
| Vault       | 8200  | Secret management (dev mode) |
| Redis       | 6379  | Distributed cache            |

### Connection Details

**MongoDB:** `mongodb://admin:password@localhost:27017`  
**Kafka:** `localhost:9092` (topics auto-created)  
**Vault:** `http://localhost:8200` (root token: `myroot`, dev mode)  
**Redis:** `localhost:6379` (password: `redispassword`)

### Building Individual Services

```bash
docker-compose build products
docker-compose build billing
docker-compose build eureka-server
docker-compose build api-gateway
```

### Data Persistence

| Service | Volume         |
|---------|----------------|
| MongoDB | `mongodb_data` |
| Kafka   | `kafka_data`   |
| Vault   | `vault_data`   |
| Redis   | `redis_data`   |
