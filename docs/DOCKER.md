# Docker Compose Setup

## External Dependencies

This project uses Docker Compose to manage external dependencies.

### Services Included

| Service | Port  | Notes               |
|---------|-------|---------------------|
| MongoDB | 27017 | Document database   |
| Kafka   | 9092  | Event streaming     |
| Vault   | 8200  | Secret management   |

### Quick Start

```bash
docker-compose up -d
```

### Connection Details

**MongoDB:** `mongodb://admin:password@localhost:27017`  
**Kafka:** `localhost:9092` (topics auto-created)  
**Vault:** `http://localhost:8200` (root token: `myroot`, dev mode)

### Data Persistence

- MongoDB: `mongodb_data` volume
- Kafka: `kafka_data` volume
- Vault: `vault_data` + `vault_config` volumes (dev mode — not persistent)
