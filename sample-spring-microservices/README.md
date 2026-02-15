# Taxi Reservation System - Microservices Architecture

A comprehensive taxi reservation system built with Spring Boot microservices, featuring user registration, driver management, trip booking with fare calculation, payment processing, reviews, and email notifications.

## Architecture

This system implements a microservices architecture with the following services:

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              Client Application                                  │
│                         (Mobile App / Web Frontend)                              │
└────────────────────────────────┬────────────────────────────────────────────────┘
                                 │
                                 │ HTTP/REST + JWT
                                 ▼
                    ┌────────────────────────┐
                    │    Keycloak (8080)     │
                    │   OAuth2/JWT Server    │
                    └────────────────────────┘
                                 │
                                 │ JWT Validation
                                 ▼
                    ┌────────────────────────┐
                    │  API Gateway (8000)    │
                    │  Spring Cloud Gateway  │
                    │  - Authentication      │
                    │  - Routing             │
                    └────────┬───────────────┘
                             │
         ┌───────────────────┼───────────────────┬────────────────┐
         │                   │                   │                │
         ▼                   ▼                   ▼                ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  User Service   │ │ Driver Service  │ │  Trip Service   │ │ Review Service  │
│    (8081)       │ │     (8082)      │ │     (8083)      │ │     (8085)      │
│                 │ │                 │ │                 │ │                 │
│ - Registration  │ │ - Registration  │ │ - Fare Calc     │ │ - Reviews       │
│ - Card Mgmt     │ │ - Car Details   │ │ - Reservations  │ │ - Ratings       │
│ - Profile       │ │ - Location      │ │ - History       │ │ - Summaries     │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │                   │
         │                   │                   │                   │
         ▼                   ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   user_db       │ │   driver_db     │ │    trip_db      │ │   review_db     │
│  PostgreSQL     │ │  PostgreSQL     │ │  PostgreSQL     │ │  PostgreSQL     │
│   (5432)        │ │   (5433)        │ │   (5434)        │ │   (5436)        │
└─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
                             │                   │
                             │                   │
                             └──────┐   ┌────────┘
                                    │   │
                                    ▼   ▼
                         ┌──────────────────────┐
                         │   Apache Kafka       │
                         │      (9092)          │
                         │                      │
                         │ Topics:              │
                         │ - trip-completed     │
                         │ - payment-processed  │
                         │ - driver-location    │
                         └──────┬───────┬───────┘
                                │       │
                    ┌───────────┘       └───────────┐
                    │                               │
                    ▼                               ▼
         ┌─────────────────────┐       ┌─────────────────────┐
         │  Payment Service    │       │ Notification Service│
         │      (8084)         │       │      (8086)         │
         │                     │       │                     │
         │ - Payment Process   │       │ - Email Sender      │
         │ - Kafka Consumer    │       │ - Kafka Consumer    │
         │ - Kafka Producer    │       │ - Trip Summaries    │
         └──────────┬──────────┘       └─────────────────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │    payment_db       │
         │    PostgreSQL       │
         │      (5435)         │
         └─────────────────────┘

Legend:
───▶  Synchronous HTTP/REST Communication
═══▶  Asynchronous Kafka Events
```

### Communication Patterns

**Synchronous (REST/HTTP):**
- Client → API Gateway → Microservices
- All REST endpoints protected with JWT

**Asynchronous (Kafka Events):**
- Driver Service → Kafka (driver-location-update)
- Trip Service → Kafka (trip-completed)
- Payment Service ← Kafka (trip-completed) → Kafka (payment-processed)
- Notification Service ← Kafka (trip-completed)

### Services

1. **API Gateway** (Port 8000)
   - Entry point for all client requests
   - JWT/OAuth2 authentication
   - Routes requests to appropriate microservices

2. **User Service** (Port 8081)
   - User registration and authentication
   - Payment card management
   - User profile management

3. **Driver Service** (Port 8082)
   - Driver registration
   - Car details management
   - Real-time location tracking
   - Publishes location updates to Kafka

4. **Trip Service** (Port 8083)
   - Trip fare calculation (based on distance, time, and location)
   - Trip reservation and management
   - Trip history
   - Publishes trip completion events to Kafka

5. **Payment Service** (Port 8084)
   - Asynchronous payment processing
   - Listens to trip completion events from Kafka
   - Publishes payment status events

6. **Review Service** (Port 8085)
   - Driver reviews and ratings
   - Review summary and statistics
   - Average rating calculation

7. **Notification Service** (Port 8086)
   - Email notifications
   - Listens to trip completion events from Kafka
   - Sends trip summary emails to users

### Infrastructure

- **PostgreSQL**: Separate database for each service
  - user-db (Port 5432)
  - driver-db (Port 5433)
  - trip-db (Port 5434)
  - payment-db (Port 5435)
  - review-db (Port 5436)

- **Apache Kafka** (Port 9092)
  - Asynchronous communication between services
  - Topics: trip-completed, payment-processed, driver-location-update

- **Keycloak** (Port 8080)
  - OAuth2/JWT authentication server
  - User and role management

## Technologies

- Java 21
- Spring Boot 3.2.1
- Spring Cloud Gateway
- Spring Data JPA
- Spring Security with OAuth2 Resource Server
- Spring Kafka
- PostgreSQL
- Apache Kafka
- Keycloak
- Testcontainers (for integration testing)
- Lombok

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose

### Running the System

1. **Start Infrastructure Services**
   ```bash
   cd sample-spring-microservices
   docker-compose up -d
   ```

   This will start:
   - PostgreSQL databases
   - Kafka and Zookeeper
   - Keycloak

2. **Configure Keycloak**
   - Access Keycloak at http://localhost:8080
   - Login with admin/admin
   - Create a realm named "taxi"
   - Create clients for the services
   - Configure JWT settings

3. **Build All Services**
   ```bash
   mvn clean install
   ```

4. **Start Services** (in separate terminals or as background processes)
   ```bash
   # API Gateway
   cd api-gateway && mvn spring-boot:run

   # User Service
   cd user-service && mvn spring-boot:run

   # Driver Service
   cd driver-service && mvn spring-boot:run

   # Trip Service
   cd trip-service && mvn spring-boot:run

   # Payment Service
   cd payment-service && mvn spring-boot:run

   # Review Service
   cd review-service && mvn spring-boot:run

   # Notification Service
   cd notification-service && mvn spring-boot:run
   ```

## API Endpoints

All requests go through the API Gateway at `http://localhost:8000`

### User Service

- `POST /api/users/register` - Register new user (Public)
- `POST /api/users/{userId}/payment-card` - Add payment card (Authenticated)
- `GET /api/users/{userId}` - Get user details (Authenticated)
- `GET /api/users/me` - Get current user (Authenticated)

### Driver Service

- `POST /api/drivers/register` - Register new driver (Public)
- `POST /api/drivers/{driverId}/location` - Update driver location (Authenticated)
- `PUT /api/drivers/{driverId}/active?active=true` - Set driver active status (Authenticated)
- `GET /api/drivers/active` - Get all active drivers (Authenticated)
- `GET /api/drivers/{driverId}` - Get driver details (Authenticated)

### Trip Service

- `POST /api/trips` - Request a new trip (Authenticated)
- `PUT /api/trips/{tripId}/accept?driverId={id}` - Accept trip (Authenticated)
- `PUT /api/trips/{tripId}/start` - Start trip (Authenticated)
- `PUT /api/trips/{tripId}/complete?userEmail={email}` - Complete trip (Authenticated)
- `GET /api/trips/{tripId}` - Get trip details (Authenticated)
- `GET /api/trips/user/{userId}` - Get user trip history (Authenticated)
- `GET /api/trips/driver/{driverId}` - Get driver trip history (Authenticated)

### Review Service

- `POST /api/reviews` - Create driver review (Authenticated)
- `GET /api/reviews/driver/{driverId}` - Get driver reviews (Authenticated)
- `GET /api/reviews/driver/{driverId}/summary` - Get driver review summary (Authenticated)

## Fare Calculation

The system calculates fares based on:

- **Base Fare**: $5.00
- **Distance Cost**: $2.50 per km
- **Peak Hours** (7-9 AM, 5-7 PM): 1.5x multiplier
- **Night Hours** (10 PM - 6 AM): 1.3x multiplier

Distance is calculated using the Haversine formula based on GPS coordinates.

## Testing

Each service includes integration tests using Testcontainers:

```bash
# Run tests for a specific service
cd user-service
mvn test

# Run tests for all services
mvn test
```

## Event-Driven Architecture

The system uses Kafka for asynchronous communication:

1. **Driver Location Updates**: Driver service publishes location updates
2. **Trip Completion**: Trip service publishes when trip is completed
3. **Payment Processing**: Payment service listens to trip completions and processes payments
4. **Email Notifications**: Notification service listens to trip completions and sends emails

## Security

- All endpoints (except registration) require JWT authentication
- JWT tokens are validated against Keycloak
- API Gateway handles authentication before routing requests
- Each service validates JWT tokens independently

## Database Schema

Each service maintains its own database following the database-per-service pattern:

- **user-db**: users, payment_cards
- **driver-db**: drivers, cars, driver_locations
- **trip-db**: trips
- **payment-db**: payments
- **review-db**: reviews

## Future Enhancements

- Service discovery with Eureka
- Circuit breaker with Resilience4j
- Distributed tracing with Zipkin
- Centralized configuration with Spring Cloud Config
- API documentation with Swagger/OpenAPI
- Real-time driver matching algorithm
- WebSocket for real-time updates
- Admin dashboard
- Mobile app integration

## License

This is a sample project for educational purposes.
