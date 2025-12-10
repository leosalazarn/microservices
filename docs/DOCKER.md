# Docker Compose Setup

## External Dependencies

This project uses Docker Compose to manage external dependencies like MongoDB, Kafka, and Vault.

### Services Included

- **MongoDB**: Document database (Port 27017)
- **Kafka**: Event streaming platform (Port 9092)
- **Vault**: Secret management (Port 8200)

### Quick Start

```bash
# Start all external dependencies
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (data will be lost)
docker-compose down -v
```

### Individual Service Management

```bash
# Start only MongoDB
docker-compose up -d mongodb

# Start only Kafka
docker-compose up -d kafka

# Start only Vault
docker-compose up -d vault

# Stop specific service
docker-compose stop mongodb
```

### Connection Details

**MongoDB:**
- URL: `mongodb://admin:password@localhost:27017`
- Username: `admin`
- Password: `password`

**Kafka:**
- Bootstrap Servers: `localhost:9092`
- Topics: Auto-created as needed

**Vault:**
- URL: `http://localhost:8200`
- Root Token: `myroot`
- Mode: Development (unsealed by default)

### Vault Usage

```bash
# Set environment variables
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='myroot'

# Store secrets
vault kv put secret/mongodb username=admin password=securepassword
vault kv put secret/kafka bootstrap-servers=localhost:9092

# Retrieve secrets
vault kv get secret/mongodb
vault kv get -field=password secret/mongodb
```

### Data Persistence

- MongoDB data: `mongodb_data` volume
- Kafka data: `kafka_data` volume
- Vault data: `vault_data` volume (dev mode - not persistent)
- Vault config: `vault_config` volume

**Note**: Vault runs in development mode for simplicity. For production, use proper Vault configuration with persistent storage and proper authentication.
