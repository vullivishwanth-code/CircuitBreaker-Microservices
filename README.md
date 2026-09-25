# CircuitBreaker - Cloud-Native E-Commerce API Gateway

A Spring Boot microservices project demonstrating fault tolerance, service discovery, API Gateway routing, resilience patterns, distributed tracing, and real-time service monitoring.

This project was developed as part of a Spring Boot Microservices Internship Project.

## Project Overview

The system simulates a cloud-native e-commerce backend consisting of multiple independent microservices connected through an API Gateway.

The architecture demonstrates how a distributed application can remain stable when individual services become slow, overloaded, or unavailable.

## Architecture

```text
                    Client / React Dashboard
                              |
                              v
                     API Gateway :8080
                              |
              +---------------+---------------+
              |               |               |
              v               v               v
        Product Service  Inventory Service  Recommendation Service
            :8081            :8082              :8083
              |               |                  |
              +---------------+------------------+
                              |
                              v
                    Eureka Registry :8761

                    Distributed Tracing
                              |
                              v
                         Zipkin :9411
```

## Microservices

| Component | Port | Purpose |
|---|---:|---|
| API Gateway | 8080 | Central entry point and resilience layer |
| Product Service | 8081 | Product catalog operations |
| Inventory Service | 8082 | Inventory information |
| Recommendation Service | 8083 | Product recommendations |
| Eureka Server | 8761 | Service discovery |
| React Dashboard | 5173/5174 | Monitoring and resilience UI |
| Zipkin | 9411 | Distributed tracing |

## Technologies Used

### Backend

- Java
- Spring Boot
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Resilience4j
- Bucket4j
- Micrometer Tracing
- Zipkin
- Maven

### Frontend

- React
- Vite
- JavaScript
- CSS

### Development Tools

- IntelliJ IDEA
- Visual Studio Code
- Git
- GitHub
- Docker
- PowerShell

## Resilience Features

### Circuit Breaker

Resilience4j Circuit Breakers protect downstream microservices from repeated failures.

The system supports states such as:

```text
CLOSED -> OPEN -> HALF_OPEN -> CLOSED
```

Fallback responses are returned when protected services are unavailable.

### Retry

Failed Product Service requests can automatically be retried before returning a failure response.

The Product route is configured with retry behavior and backoff.

### Rate Limiting

The API Gateway protects Product Service endpoints from excessive requests using Bucket4j.

When the configured request limit is exceeded, the Gateway returns:

```text
HTTP 429 Too Many Requests
```

### Bulkhead

Bulkhead isolation limits the number of concurrent requests reaching protected backend operations.

This prevents one overloaded operation from consuming all available resources.

### Timeout Protection

Slow backend operations are protected by timeout mechanisms so requests do not wait indefinitely.

A dedicated slow Product endpoint is included for resilience testing.

Example:

```text
/api/products/1/slow
```

### Graceful Fallback

If a service becomes unavailable, the Gateway can return a controlled fallback response instead of exposing an uncontrolled backend failure.

Example:

```json
{
  "status": "SERVICE_UNAVAILABLE",
  "message": "Product Service is temporarily unavailable."
}
```

Similar fallback behavior is provided for Inventory and Recommendation services.

## Service Discovery

Eureka Server provides service registration and discovery.

The Gateway routes requests using registered service names rather than depending only on fixed service addresses.

Examples:

```text
product-service
inventory-service
recommendation-service
```

## Distributed Tracing

Micrometer Tracing and Zipkin are used to observe requests across the distributed system.

A request can be traced through components such as:

```text
Client
   |
API Gateway
   |
Product Service
```

Zipkin provides trace IDs, spans, request duration, and service-to-service visibility.

Zipkin UI:

```text
http://localhost:9411/zipkin/
```

## Monitoring Dashboard

The project contains a React-based monitoring dashboard called **ResilienceHub**.

The dashboard provides visual information about:

- API Gateway health
- Product Service health
- Inventory Service health
- Recommendation Service health
- Eureka availability
- Circuit Breaker state
- Rate Limiter configuration
- Bulkhead protection
- Timeout protection
- Request testing
- Service degradation
- Service recovery

When a service is stopped, the dashboard can display a degraded system state.

After the service is restarted and becomes healthy again, the dashboard returns to:

```text
All Systems Operational
```

## Request Logging

The API Gateway records incoming and completed requests.

Example:

```text
Request ID: <UUID> | Incoming request: GET /api/products/1

Request ID: <UUID> | Completed request: GET /api/products/1 | Status: 200 | Time: <duration>
```

Request IDs make it easier to correlate logs belonging to the same request.

## Chaos / Failure Testing

The system supports manual failure simulation.

Example procedure:

1. Start all services.
2. Verify the dashboard reports healthy services.
3. Stop one backend service.
4. Send requests through the API Gateway.
5. Observe fallback/degraded behavior.
6. Observe the dashboard reporting the unavailable service.
7. Restart the service.
8. Run the health check again.
9. Verify the dashboard returns to the operational state.

This demonstrates service failure detection and recovery.

## Example API Endpoints

### Product

```text
GET http://localhost:8080/api/products/1
```

### Slow Product Test

```text
GET http://localhost:8080/api/products/1/slow
```

### Inventory

```text
GET http://localhost:8080/api/inventory/1
```

### Recommendation

```text
GET http://localhost:8080/api/recommendations/1
```

## Running the Project

Start the components in the following order:

```text
1. Eureka Service Registry
2. Product Service
3. Inventory Service
4. Recommendation Service
5. API Gateway
6. React Dashboard
7. Zipkin
```

Start the React dashboard from:

```text
frontend-dashboard
```

Run:

```bash
npm install
npm run dev
```

For Zipkin, Docker can be used:

```bash
docker run -d -p 9411:9411 --name zipkin openzipkin/zipkin
```

Then open the dashboard using the URL displayed by Vite, normally:

```text
http://localhost:5173
```

or another available development port such as:

```text
http://localhost:5174
```

## Key Learning Outcomes

This project demonstrates practical understanding of:

1. Microservices architecture
2. Spring Boot REST APIs
3. Spring Cloud Gateway
4. Eureka service discovery
5. Circuit Breaker pattern
6. Retry pattern
7. Rate limiting
8. Bulkhead isolation
9. Timeout protection
10. Graceful fallback handling
11. Distributed tracing with Zipkin
12. Request correlation and logging
13. React-based service monitoring
14. Failure and recovery testing
15. Git and GitHub project management

## Author

**Vishwanth Vulli**

Spring Boot Microservices Internship Project