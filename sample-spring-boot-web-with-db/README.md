# Sample Spring Boot Web Application with Database

A production-ready Spring Boot 4 application with PostgreSQL, JWT/OAuth2 security, Kubernetes deployment, and CI/CD pipeline.

## Features

- **Spring Boot 4.0.0-M2** with Java 25
- **Person Entity** with full CRUD operations
- **JWT/OAuth2 Security** protecting all REST endpoints
- **PostgreSQL** database with Liquibase schema management
- **OpenAPI/Swagger** documentation
- **Testcontainers** integration tests
- **Kubernetes** deployment manifests
- **Skaffold** for container building with Jib
- **CircleCI** pipeline with Kind cluster testing

## Technology Stack

- Spring Boot 4
- Spring Data JPA
- Spring Security with OAuth2 Resource Server
- PostgreSQL
- Liquibase
- Springdoc OpenAPI
- Testcontainers
- Maven
- Jib
- Skaffold
- Kubernetes
- CircleCI

## API Endpoints

All endpoints require JWT Bearer token authentication (except health and Swagger UI).

- `GET /api/persons` - Get all persons
- `GET /api/persons/{id}` - Get person by ID
- `POST /api/persons` - Create new person
- `PUT /api/persons/{id}` - Update person
- `DELETE /api/persons/{id}` - Delete person

## Running Locally

### Prerequisites

- Java 25
- Docker (for PostgreSQL)
- Maven

### Start PostgreSQL

```bash
docker run --name postgres -e POSTGRES_DB=sampledb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:17
```

### Run the Application

```bash
./mvnw spring-boot:run
```

### Access Swagger UI

http://localhost:8080/swagger-ui.html

### Access API Docs

http://localhost:8080/api-docs

## Running Tests

```bash
./mvnw test
```

Tests use Testcontainers to spin up PostgreSQL automatically.

## Deploying to Kubernetes

### Prerequisites

- Docker
- kubectl
- Skaffold

### Deploy with Skaffold

```bash
skaffold dev
```

This will:
1. Build the Docker image using Jib
2. Deploy PostgreSQL to Kubernetes
3. Deploy the application to Kubernetes
4. Set up port forwarding to localhost:8080

### Manual Kubernetes Deployment

```bash
kubectl apply -f k8s/
```

## Configuration

### Environment Variables

- `DB_HOST` - PostgreSQL host (default: localhost)
- `DB_PORT` - PostgreSQL port (default: 5432)
- `DB_NAME` - Database name (default: sampledb)
- `DB_USER` - Database username (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)
- `JWT_ISSUER_URI` - OAuth2 issuer URI
- `JWT_JWK_SET_URI` - JWK Set URI for JWT validation

### JWT Configuration

For production, configure your OAuth2 provider:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-oauth-provider.com
          jwk-set-uri: https://your-oauth-provider.com/.well-known/jwks.json
```

## CI/CD Pipeline

The CircleCI pipeline:

1. **Build and Test** - Compiles code and runs all tests
2. **Deploy to K8s** - Creates Kind cluster, deploys with Skaffold, verifies deployment

## Security

- All REST endpoints protected with JWT/OAuth2
- Passwords stored in Kubernetes Secrets
- Stateless session management
- CSRF disabled (for stateless API)

## Database Schema

Managed by Liquibase. Initial schema creates `persons` table with:

- `id` - Primary key (auto-generated)
- `first_name` - VARCHAR(100)
- `last_name` - VARCHAR(100)
- `email` - VARCHAR(255) - Unique
- `date_of_birth` - DATE
- `phone_number` - VARCHAR(20)
- `address` - VARCHAR(500)

## License

Apache 2.0
