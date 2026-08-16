\# CircuitBreaker Microservices



A Spring Boot microservices project demonstrating service discovery, API Gateway routing, service-to-service communication, and resilience patterns including Circuit Breaker, Rate Limiting, Bulkhead Isolation, Timeout Handling, and Fallback Responses.



\## Project Overview



This project demonstrates how a distributed microservices application can remain stable and responsive when individual services become slow, overloaded, or temporarily unavailable.



The system contains five major components:



\- Service Registry

\- Product Service

\- Inventory Service

\- Recommendation Service

\- API Gateway



The Product Service communicates with the Inventory Service and Recommendation Service to build a combined product response.



The API Gateway acts as the centralized entry point and applies resilience mechanisms to protect downstream services.



\---



\## Architecture



```text

&#x20;                        CLIENT

&#x20;                           |

&#x20;                           v

&#x20;                    +-------------+

&#x20;                    | API Gateway |

&#x20;                    |  Port 8080  |

&#x20;                    +-------------+

&#x20;                           |

&#x20;            +--------------+--------------+

&#x20;            |                             |

&#x20;            v                             v

&#x20;     Request Routing              Resilience Layer

&#x20;                                  - Circuit Breaker

&#x20;                                  - Rate Limiter

&#x20;                                  - Bulkhead

&#x20;                                  - TimeLimiter

&#x20;            |

&#x20;            v

&#x20;     +-----------------+

&#x20;     | Product Service |

&#x20;     |    Port 8081    |

&#x20;     +-----------------+

&#x20;            |

&#x20;       +----+----+

&#x20;       |         |

&#x20;       v         v

+---------------+   +------------------------+

|   Inventory   |   | Recommendation Service |

|    Service    |   |       Port 8083        |

|   Port 8082   |   +------------------------+

+---------------+

&#x20;       |

&#x20;       +-------------------+

&#x20;                           |

&#x20;                           v

&#x20;                   +----------------+

&#x20;                   | Eureka Service |

&#x20;                   |    Registry    |

&#x20;                   |   Port 8761    |

&#x20;                   +----------------+

```



\---



\## Technologies Used



\- Java 21

\- Spring Boot

\- Spring Cloud

\- Spring Cloud Gateway

\- Netflix Eureka

\- Resilience4j

\- Bucket4j

\- Caffeine Cache

\- Maven

\- REST APIs

\- Git

\- GitHub



\---



\## Microservices



\### 1. Service Registry



The Service Registry uses Netflix Eureka for service discovery.



Instead of depending entirely on hardcoded service addresses, microservices can register themselves with Eureka and discover other services dynamically.



\*\*Port:\*\*



```text

8761

```



Eureka Dashboard:



```text

http://localhost:8761

```



\---



\### 2. Product Service



The Product Service handles product-related operations.



It also communicates with:



\- Inventory Service

\- Recommendation Service



to provide aggregated product information.



\*\*Port:\*\*



```text

8081

```



Example endpoints:



```http

GET /api/products

GET /api/products/{id}

GET /api/products/{id}/details

```



The details endpoint combines information from multiple microservices:



```text

Product Information

&#x20;       +

Inventory Information

&#x20;       +

Recommendation Information

&#x20;       =

Combined Product Response

```



Example response:



```json

{

&#x20; "product": {

&#x20;   "id": 1,

&#x20;   "name": "Apple MacBook Pro 14",

&#x20;   "category": "Laptops",

&#x20;   "price": 1999.99,

&#x20;   "stock": 12

&#x20; },

&#x20; "inventory": {

&#x20;   "productId": 1,

&#x20;   "availableQuantity": 12,

&#x20;   "warehouseLocation": "Dallas Warehouse"

&#x20; },

&#x20; "recommendations": {

&#x20;   "recommendations": \[

&#x20;     "Top Sellers",

&#x20;     "Trending Products",

&#x20;     "Customers Also Viewed"

&#x20;   ],

&#x20;   "productId": 1

&#x20; }

}

```



\---



\### 3. Inventory Service



The Inventory Service provides inventory-related information such as product availability and warehouse data.



\*\*Port:\*\*



```text

8082

```



It allows the Product Service to retrieve inventory information independently from the product domain.



\---



\### 4. Recommendation Service



The Recommendation Service provides recommendation information for products.



\*\*Port:\*\*



```text

8083

```



This demonstrates how a product-facing service can retrieve additional information from an independent downstream microservice.



\---



\### 5. API Gateway



The API Gateway acts as the main entry point into the microservices architecture.



\*\*Port:\*\*



```text

8080

```



Instead of clients communicating directly with every internal service, requests can pass through the Gateway.



The Gateway provides:



\- Centralized routing

\- Eureka-based service discovery

\- Load-balanced routing

\- Circuit Breaker protection

\- Rate Limiting

\- Bulkhead isolation

\- Timeout protection

\- Fallback handling



\---



\# Resilience Patterns



A major objective of this project is demonstrating how microservices behave when downstream dependencies experience failures or delays.



\## Circuit Breaker



The Circuit Breaker pattern protects the application from repeatedly calling an unhealthy or slow downstream service.



Conceptually:



```text

CLOSED

&#x20;  |

Failures exceed threshold

&#x20;  |

&#x20;  v

OPEN

&#x20;  |

Wait / recovery period

&#x20;  |

&#x20;  v

HALF\_OPEN

&#x20;  |

Successful requests

&#x20;  |

&#x20;  v

CLOSED

```



\### CLOSED



Requests are allowed normally.



\### OPEN



Requests to the unhealthy service are temporarily prevented.



\### HALF\_OPEN



A limited number of requests are allowed to determine whether the downstream service has recovered.



This helps prevent cascading failures across the application.



\---



\## Timeout Handling



A downstream service should not be allowed to block a request indefinitely.



The project demonstrates timeout protection for slow service responses.



Example scenario:



```text

Product Service delay = 5 seconds

Gateway timeout       = 3 seconds

```



Instead of waiting indefinitely, the Gateway can stop waiting and return a controlled response.



Example fallback response:



```json

{

&#x20; "status": "SERVICE\_UNAVAILABLE",

&#x20; "message": "Product Service is taking too long. Please try again."

}

```



\---



\## Rate Limiting



Rate limiting protects the API from excessive request traffic.



The project uses Bucket4j for rate limiting.



Example configuration:



```text

5 requests every 10 seconds

```



When the allowed request limit is exceeded, the client receives:



```text

HTTP 429 Too Many Requests

```



This protects backend services from excessive traffic.



\---



\## Bulkhead Pattern



The Bulkhead pattern limits the number of requests that can simultaneously use a protected resource.



Example configuration:



```text

Maximum concurrent calls: 3

```



Conceptually:



```text

Incoming Requests

&#x20;     |

&#x20;     v

+----------------+

|    Bulkhead    |

| Max Calls = 3  |

+----------------+

&#x20;  |    |    |

&#x20;  v    v    v

&#x20;Req1 Req2 Req3



Additional requests

&#x20;       |

&#x20;       v

&#x20;Controlled rejection

```



This prevents one overloaded component from consuming all available resources.



\---



\## Service Discovery



All services can register with Eureka.



The Service Registry provides a centralized location for discovering running service instances.



Conceptually:



```text

Product Service --------+

Inventory Service ------+

Recommendation Service -+----> Eureka Registry

API Gateway ------------+

```



This allows the architecture to use logical service identities rather than relying only on fixed network addresses.



\---



\## Service-to-Service Communication



The Product Service communicates with downstream services to create a combined response.



```text

Product Service

&#x20;     |

&#x20;     +--------> Inventory Service

&#x20;     |

&#x20;     +--------> Recommendation Service

```



The responses are combined before being returned to the client.



\---



\# Failure Handling



\## Normal Flow



```text

Client

&#x20; |

&#x20; v

API Gateway

&#x20; |

&#x20; v

Product Service

&#x20; |

&#x20; +------> Inventory Service

&#x20; |

&#x20; +------> Recommendation Service

&#x20; |

&#x20; v

Successful Response

```



\## Slow or Failed Service



```text

Client

&#x20; |

&#x20; v

API Gateway

&#x20; |

&#x20; v

Resilience Protection

&#x20; |

&#x20; X

Slow / Unavailable Service

&#x20; |

&#x20; v

Fallback Response

```



\## Recovery



After the downstream service becomes healthy again:



```text

Client

&#x20; |

&#x20; v

API Gateway

&#x20; |

&#x20; v

Product Service

&#x20; |

&#x20; v

Normal Response Restored

```



This demonstrates graceful degradation and recovery in a distributed application.



\---



\# Running the Project



\## Prerequisites



Make sure the following are installed:



\- Java 21

\- Maven

\- Git



\---



\## Startup Order



Start the applications in this order:



```text

1\. Service Registry

2\. Inventory Service

3\. Recommendation Service

4\. Product Service

5\. API Gateway

```



\---



\## Step 1 - Start Service Registry



Start the Eureka Service Registry.



Then open:



```text

http://localhost:8761

```



Verify that the Eureka dashboard is running.



\---



\## Step 2 - Start Inventory Service



Start the Inventory Service.



Expected port:



```text

8082

```



\---



\## Step 3 - Start Recommendation Service



Start the Recommendation Service.



Expected port:



```text

8083

```



\---



\## Step 4 - Start Product Service



Start the Product Service.



Expected port:



```text

8081

```



\---



\## Step 5 - Start API Gateway



Finally start the API Gateway.



Expected port:



```text

8080

```



\---



\# Testing



Once all services are running, verify them in the Eureka Dashboard.



Then test the application through the API Gateway.



Example:



```text

http://localhost:8080/api/products/1/details

```



The expected flow is:



```text

Client

&#x20; |

&#x20; v

API Gateway

&#x20; |

&#x20; v

Product Service

&#x20; |

&#x20; +---- Inventory Service

&#x20; |

&#x20; +---- Recommendation Service

&#x20; |

&#x20; v

Combined Response

```



\---



\# Project Structure



```text

CircuitBreaker-Microservices/

|

|-- service-registry/

|

|-- product-service/

|

|-- inventory-service/

|

|-- recommendation-service/

|

|-- api-gateway/

|

|-- .gitignore

|

`-- README.md

```



Each microservice is maintained as an independent Spring Boot application.



\---



\# Git Commit History



The repository was developed using feature-based commits.



```text

feat: set up Eureka service registry

feat: add product microservice

feat: add inventory microservice

feat: add recommendation microservice

feat: add API gateway with resilience configurations

docs: add comprehensive project README

```



This makes the development history easier to understand and review.



\---



\# Key Concepts Demonstrated



This project demonstrates practical implementation of:



\- Microservices Architecture

\- Spring Boot

\- REST API Development

\- Service Discovery

\- Netflix Eureka

\- API Gateway

\- Service-to-Service Communication

\- Circuit Breaker Pattern

\- Rate Limiting

\- Bulkhead Pattern

\- Timeout Handling

\- Fallback Responses

\- Graceful Degradation

\- Failure Recovery

\- Distributed Application Design

\- Git Version Control



\---



\# Why Resilience Matters



In a distributed system, failures are expected.



A service may:



\- Become unavailable

\- Respond slowly

\- Receive excessive traffic

\- Exhaust resources

\- Experience temporary network problems



Without resilience mechanisms, failure in one service can affect multiple dependent services.



This project demonstrates techniques for reducing that risk.



```text

Failure

&#x20;  |

&#x20;  v

Resilience Layer

&#x20;  |

&#x20;  +---- Circuit Breaker

&#x20;  |

&#x20;  +---- Timeout

&#x20;  |

&#x20;  +---- Rate Limiter

&#x20;  |

&#x20;  +---- Bulkhead

&#x20;  |

&#x20;  v

Controlled Response

```



\---



\# Future Improvements



Potential future enhancements include:



\- Docker containerization

\- Docker Compose

\- Centralized configuration

\- Spring Cloud Config Server

\- Prometheus metrics

\- Grafana dashboards

\- Distributed tracing

\- Centralized logging

\- Database persistence

\- Authentication and authorization

\- Automated integration testing

\- CI/CD pipeline

\- Cloud deployment

\- Kubernetes deployment



\---



\# Learning Outcomes



Through this project, the following concepts were explored:



1\. Designing independent Spring Boot microservices.

2\. Registering services using Eureka.

3\. Implementing service discovery.

4\. Creating REST APIs.

5\. Implementing service-to-service communication.

6\. Building a centralized API Gateway.

7\. Protecting services using Circuit Breaker patterns.

8\. Handling slow responses with timeout mechanisms.

9\. Protecting APIs using rate limiting.

10\. Isolating concurrent requests using the Bulkhead pattern.

11\. Implementing graceful fallback behavior.

12\. Managing a multi-service project using Git and GitHub.



\---



\# Author



\*\*Vishwanth Vulli\*\*



Spring Boot Microservices Internship Project

