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

| Service     | Port  | Notes                                    |
|-------------|-------|------------------------------------------|
| API Gateway | 8080  | Entry point for all requests             |
| Products    | 8081  | Product management + CQRS                |
| Billing     | 8082  | Invoice management + SAGA                |
| Eureka      | 8761  | Service discovery                        |
| MongoDB     | 27017 | Document database                        |
| Kafka       | 9092  | Event streaming                          |
| Vault       | 8200  | Secret management (dev mode)             |
| Redis       | 6379  | Distributed cache                        |
| Zipkin      | 9411  | Distributed tracing (Micrometer + Brave) |
| Kafdrop     | 9000  | Kafka topic browser                      |
| Kafka UI    | 8090  | Kafka management UI                      |

### Connection Details

**MongoDB:** `mongodb://admin:password@localhost:27017`  
**Kafka:** `localhost:9092` (external), `kafka:19092` (internal Docker network)  
**Vault:** `http://localhost:8200` (root token: `myroot`, dev mode)  
**Redis:** `localhost:6379` (password: `redispassword`)  
**Zipkin:** `http://localhost:9411`

### Notes

- **Kafka internal port**: Containers on the Docker network connect via `kafka:19092`.
  External clients use `localhost:9092`.
- **Alpine base images**: Services use `eclipse-temurin:21-jre-alpine`. Native libraries
  (e.g., snappy compression) are unavailable — Kafka is configured with `gzip` compression
  (pure Java) instead.

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
