# API Guide — gRPC Microservices Portfolio

This document provides a complete guide to using and understanding the API.

---

## Base URL

```
http://localhost:8080
```

All requests go through the **API Gateway** which handles routing to the correct microservice.

---

## Understanding the Flow

```
Your Request (HTTP/JSON)
        ↓
  API Gateway (8080)
  [Converts JSON → Protobuf]
        ↓
  gRPC Call (binary, HTTP/2)
        ↓
  Microservice (User or Order)
  [Processes & returns Protobuf]
        ↓
  API Gateway
  [Converts Protobuf → JSON]
        ↓
Your Response (HTTP/JSON)
```

---

## Users API

### Create User
```http
POST /api/users
Content-Type: application/json

{
  "name": "Budi Santoso",
  "email": "budi@example.com"
}
```

**Response 201:**
```json
{
  "user": {
    "id": "4",
    "name": "Budi Santoso",
    "email": "budi@example.com",
    "created_at": "2024-01-15T10:30:00Z",
    "updated_at": "2024-01-15T10:30:00Z"
  },
  "message": "User created successfully"
}
```

---

### Get All Users
```http
GET /api/users?page=0&pageSize=10
```

**Response 200:**
```json
{
  "users": [
    { "id": "1", "name": "Budi Santoso", "email": "budi@example.com", ... },
    { "id": "2", "name": "Siti Rahayu",  "email": "siti@example.com",  ... }
  ],
  "total": 2
}
```

---

### Get User by ID
```http
GET /api/users/1
```

**Response 200:** Single user object  
**Response 404:** `{ "error": "NOT_FOUND", "message": "User not found with ID: 1" }`

---

### Update User
```http
PUT /api/users/1
Content-Type: application/json

{
  "name": "Budi Santoso Updated",
  "email": "budi.new@example.com"
}
```

---

### Delete User
```http
DELETE /api/users/1
```

**Response 200:**
```json
{ "success": true, "message": "User deleted successfully" }
```

---

## Orders API

### Create Order
```http
POST /api/orders
Content-Type: application/json

{
  "userId": "1",
  "productName": "Laptop ASUS ROG",
  "quantity": 1,
  "price": 15000000
}
```

**Response 201:**
```json
{
  "order": {
    "id": "4",
    "user_id": "1",
    "product_name": "Laptop ASUS ROG",
    "quantity": 1,
    "price": 15000000.0,
    "status": "PENDING",
    "created_at": "2024-01-15T10:30:00Z",
    "updated_at": "2024-01-15T10:30:00Z"
  },
  "message": "Order created successfully"
}
```

---

### Get Orders by User
```http
GET /api/orders/user/1
```

**Response 200:**
```json
{
  "orders": [ ... ],
  "total": 2
}
```

---

### Update Order Status
```http
PUT /api/orders/1/status
Content-Type: application/json

{
  "status": "SHIPPED"
}
```

**Valid statuses:** `PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` (or `CANCELLED`)

---

## gRPC Direct Access

You can also call the gRPC services directly using [grpcurl](https://github.com/fullstorydev/grpcurl):

```bash
# List available services
grpcurl -plaintext localhost:9000 list

# Call CreateUser
grpcurl -plaintext -d '{"name": "Test User", "email": "test@example.com"}' \
  localhost:9000 user.UserService/CreateUser

# Call GetUser
grpcurl -plaintext -d '{"id": "1"}' \
  localhost:9000 user.UserService/GetUser

# Call ListUsers
grpcurl -plaintext -d '{"page": 0, "page_size": 10}' \
  localhost:9000 user.UserService/ListUsers

# Create Order via gRPC
grpcurl -plaintext -d '{"user_id": "1", "product_name": "Laptop", "quantity": 1, "price": 5000000}' \
  localhost:9001 order.OrderService/CreateOrder

# Update Order Status
grpcurl -plaintext -d '{"id": "1", "status": "CONFIRMED"}' \
  localhost:9001 order.OrderService/UpdateOrderStatus
```

---

## Error Responses

All errors follow this format:

```json
{
  "error": "NOT_FOUND",
  "message": "Human-readable error description",
  "status": 404
}
```

| HTTP Status | gRPC Status | Meaning |
|-------------|-------------|---------|
| 400 | INVALID_ARGUMENT | Bad request / validation error |
| 404 | NOT_FOUND | Resource not found |
| 409 | ALREADY_EXISTS | Resource conflict |
| 422 | FAILED_PRECONDITION | Business rule violation |
| 500 | INTERNAL | Server error |

---

## Service Ports

| Service | HTTP Port | gRPC Port |
|---------|-----------|-----------|
| API Gateway | 8080 | — |
| User Service | 8081 | 9000 |
| Order Service | 8082 | 9001 |
