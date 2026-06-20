# 🚀 Microservices with gRPC & Quarkus

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-4695EB?style=for-the-badge&logo=quarkus&logoColor=white)](https://quarkus.io/)
[![gRPC](https://img.shields.io/badge/gRPC-Protocol%20Buffers-244c5a?style=for-the-badge&logo=google&logoColor=white)](https://grpc.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

> A production-ready microservices architecture demonstrating inter-service communication with **gRPC**, built on **Quarkus** (Supersonic Subatomic Java). This project showcases modern backend engineering patterns for cloud-native applications.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
│                   (HTTP REST / curl)                        │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP/REST
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY                              │
│                   :8080 (REST)                              │
│              Quarkus REST Client                            │
└──────────────┬──────────────────────┬───────────────────────┘
               │ gRPC                  │ gRPC
               ▼                       ▼
┌──────────────────────┐   ┌──────────────────────┐
│    USER SERVICE      │   │   ORDER SERVICE      │
│     :9000 (gRPC)     │   │    :9001 (gRPC)      │
│     :8081 (HTTP)     │   │    :8082 (HTTP)      │
│                      │   │                      │
│  - CreateUser        │   │  - CreateOrder       │
│  - GetUser           │   │  - GetOrder          │
│  - ListUsers         │   │  - ListOrdersByUser  │
│  - UpdateUser        │   │  - UpdateOrderStatus │
│  - DeleteUser        │   │  - DeleteOrder       │
│                      │   │                      │
│  [In-Memory Store]   │   │  [In-Memory Store]   │
└──────────────────────┘   └──────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Framework** | Quarkus 3.x | Supersonic Java runtime |
| **Communication** | gRPC + Protocol Buffers | Efficient inter-service RPC |
| **API** | REST (JAX-RS) | Public-facing HTTP endpoints |
| **Serialization** | Protobuf 3 | Type-safe binary serialization |
| **Build** | Maven 3.9 | Dependency management |
| **Container** | Docker + Docker Compose | Local orchestration |
| **Java** | Java 17 | LTS runtime |

---

## 📁 Project Structure

```
grpc-microservices-portfolio/
├── proto/                          # Shared Protobuf definitions
│   ├── user.proto                  # User service contract
│   └── order.proto                 # Order service contract
│
├── user-service/                   # User microservice
│   ├── src/main/
│   │   ├── java/com/portfolio/user/
│   │   │   ├── grpc/               # gRPC service implementation
│   │   │   ├── resource/           # REST endpoints (health, metrics)
│   │   │   └── service/            # Business logic
│   │   ├── proto/                  # Local proto files
│   │   └── resources/
│   │       └── application.properties
│   └── pom.xml
│
├── order-service/                  # Order microservice
│   ├── src/main/
│   │   ├── java/com/portfolio/order/
│   │   │   ├── grpc/               # gRPC service implementation
│   │   │   ├── resource/           # REST endpoints
│   │   │   └── service/            # Business logic
│   │   ├── proto/
│   │   └── resources/
│   │       └── application.properties
│   └── pom.xml
│
├── api-gateway/                    # REST Gateway
│   ├── src/main/
│   │   ├── java/com/portfolio/gateway/
│   │   │   ├── client/             # gRPC stub clients
│   │   │   ├── dto/                # Data transfer objects
│   │   │   └── resource/           # REST controllers
│   │   ├── proto/
│   │   └── resources/
│   │       └── application.properties
│   └── pom.xml
│
├── docker-compose.yml              # Full stack orchestration
├── docs/
│   └── api-guide.md                # API documentation
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Option 1: Docker Compose (Recommended)
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/grpc-microservices-portfolio.git
cd grpc-microservices-portfolio

# Build and start all services
docker-compose up --build

# Verify services are running
curl http://localhost:8080/health
```

### Option 2: Run Each Service Manually

**Terminal 1 — User Service**
```bash
cd user-service
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

**Terminal 2 — Order Service**
```bash
cd order-service
./mvnw quarkus:dev -Dquarkus.http.port=8082
```

**Terminal 3 — API Gateway**
```bash
cd api-gateway
./mvnw quarkus:dev -Dquarkus.http.port=8080
```

---

## 📡 API Endpoints

### Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users` | Create a new user |
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PUT` | `/api/users/{id}` | Update user |
| `DELETE` | `/api/users/{id}` | Delete user |

### Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/orders` | Create a new order |
| `GET` | `/api/orders` | List all orders |
| `GET` | `/api/orders/{id}` | Get order by ID |
| `GET` | `/api/orders/user/{userId}` | Get orders by user |
| `PUT` | `/api/orders/{id}/status` | Update order status |
| `DELETE` | `/api/orders/{id}` | Delete order |

---

## 🧪 Testing the API

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Budi Santoso", "email": "budi@example.com"}'

# 2. List all users
curl http://localhost:8080/api/users

# 3. Create an order for user ID 1
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "1", "productName": "Laptop ASUS", "quantity": 1, "price": 12000000}'

# 4. Get orders by user
curl http://localhost:8080/api/orders/user/1

# 5. Update order status
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
```

---

## 🔑 Key Concepts Demonstrated

### 1. Protocol Buffers (Protobuf)
- Defining service contracts with `.proto` files
- Strongly-typed request/response messages
- Code generation for Java stubs

### 2. gRPC Server (Quarkus)
- Implementing generated `ImplBase` classes
- Unary RPC calls
- StreamObserver pattern for async responses

### 3. gRPC Client
- Using `@GrpcClient` injection
- Blocking stub calls from REST handlers
- Error propagation across service boundaries

### 4. API Gateway Pattern
- Single entry point for all client requests
- Protocol translation: REST ↔ gRPC
- Response aggregation

### 5. Cloud-Native Readiness
- Health check endpoints (`/q/health`)
- Metrics endpoint (`/q/metrics`)
- Docker-optimized builds

---

## 📊 gRPC vs REST Comparison

| Feature | gRPC | REST |
|---------|------|------|
| Protocol | HTTP/2 | HTTP/1.1 |
| Payload | Binary (Protobuf) | Text (JSON) |
| Contract | Strict (.proto) | Optional (OpenAPI) |
| Performance | ~7x faster | Baseline |
| Streaming | Native support | Polling/SSE |
| Code Gen | Auto-generated | Manual |

---

## 🏗️ Extending This Project

Ideas to add more depth to this portfolio:
- [ ] Add PostgreSQL with Panache ORM
- [ ] Implement JWT authentication in API Gateway
- [ ] Add Kafka for async event publishing
- [ ] Deploy to Kubernetes (include YAML manifests)
- [ ] Add distributed tracing with OpenTelemetry
- [ ] Write integration tests with `@QuarkusIntegrationTest`
- [ ] Add gRPC streaming endpoint

---

## 👤 Author

**[Your Name]**
- GitHub: [@your-username](https://github.com/your-username)
- LinkedIn: [your-linkedin](https://linkedin.com/in/your-linkedin)
- Email: your.email@example.com

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

> ⭐ If this helped you understand gRPC with Quarkus, please give it a star!
