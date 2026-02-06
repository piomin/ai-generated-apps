# Sample Spring Boot Web Application with PostgreSQL

A sample Spring Boot 4 application demonstrating a RESTful API with PostgreSQL database, complete with Kubernetes deployment configurations.

## Technologies Used

- **Spring Boot 4.0.0-M2** - Application framework
- **Java 25** - Programming language
- **Spring Data JPA** - Data persistence
- **PostgreSQL** - Relational database
- **Liquibase** - Database schema migration
- **Springdoc OpenAPI** - API documentation
- **Testcontainers** - Integration testing
- **Jib** - Container image building
- **Skaffold** - Kubernetes deployment automation
- **CircleCI** - CI/CD pipeline

## Features

- RESTful CRUD API for Person entities
- PostgreSQL database with Liquibase migrations
- Interactive API documentation with Swagger UI
- Comprehensive integration tests using Testcontainers
- Kubernetes-ready with health checks
- CI/CD pipeline with CircleCI
- Containerized deployment with Jib and Skaffold

## Prerequisites

- JDK 25
- Maven 3.9+
- Docker (for running tests and local deployment)
- kubectl (for Kubernetes deployment)
- Skaffold (for Kubernetes development workflow)

## Building the Application

```bash
mvn clean package
```

## Running Tests

The application includes comprehensive integration tests using Testcontainers:

```bash
mvn test
```

## Running Locally

### With Docker Compose (Recommended)

1. Start PostgreSQL:
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=sampledb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:17
```

2. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### API Documentation

Once the application is running, access the Swagger UI at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## API Endpoints

### Person REST API

- `GET /api/persons` - Get all persons
- `GET /api/persons/{id}` - Get person by ID
- `POST /api/persons` - Create a new person
- `PUT /api/persons/{id}` - Update a person
- `DELETE /api/persons/{id}` - Delete a person

### Example Request

```bash
curl -X POST http://localhost:8080/api/persons \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "dateOfBirth": "1990-01-15",
    "phoneNumber": "+1234567890",
    "address": "123 Main St"
  }'
```

## Deploying to Kubernetes

### Prerequisites

- A Kubernetes cluster (local or cloud)
- kubectl configured to connect to your cluster
- Skaffold installed

### Using Skaffold

1. Deploy to Kubernetes:
```bash
skaffold run
```

2. For development with hot reload:
```bash
skaffold dev
```

3. To delete the deployment:
```bash
skaffold delete
```

### Manual Kubernetes Deployment

1. Build the container image:
```bash
mvn compile jib:dockerBuild
```

2. Apply Kubernetes manifests:
```bash
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/deployment.yaml
```

3. Check deployment status:
```bash
kubectl get pods
kubectl get services
```

4. Access the application:
```bash
kubectl port-forward service/sample-spring-boot-service 8080:80
```

## Configuration

### Application Properties

The application can be configured using environment variables:

- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: sampledb)
- `DB_USER` - Database username (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)

### Database Schema

Database schema is managed by Liquibase. Changes are tracked in:
- `src/main/resources/db/changelog/db.changelog-master.xml`
- `src/main/resources/db/changelog/changes/`

## CI/CD Pipeline

The project includes a CircleCI configuration (`.circleci/config.yml`) that:

1. Builds the application
2. Runs all tests
3. Tests deployment to Kubernetes using Kind

## Project Structure

```
sample-spring-boot-web-with-db/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   └── DemoApplication.java
│   │   └── resources/
│   │       ├── db/changelog/   # Liquibase migrations
│   │       └── application.yml
│   └── test/
│       └── java/com/example/demo/
│           └── PersonControllerIntegrationTest.java
├── k8s/                        # Kubernetes manifests
│   ├── deployment.yaml
│   └── postgres.yaml
├── skaffold.yaml              # Skaffold configuration
├── .circleci/
│   └── config.yml             # CircleCI pipeline
└── pom.xml
```

## Health Checks

The application exposes health check endpoints via Spring Boot Actuator:

- Liveness: `/actuator/health/liveness`
- Readiness: `/actuator/health/readiness`

## Notes

- Tests use Hibernate's `ddl-auto=create-drop` for schema generation
- Production uses Liquibase for database migrations
- The application uses Spring Boot 4.0.0-M2 (milestone release) with Java 25
- Tests require Docker to run Testcontainers

## License

This is a sample application for demonstration purposes.
