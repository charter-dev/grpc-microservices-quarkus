package com.portfolio.gateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Objects (DTOs) for the API Gateway REST layer.
 *
 * These classes:
 * - Define the JSON shape that REST clients see (public API contract)
 * - Are separate from Protobuf-generated classes (internal contract)
 * - Provide validation annotations and documentation
 *
 * Why separate DTOs?
 * - Protobuf classes have getters like getName() but also generated fields
 *   that would pollute the JSON output
 * - DTOs let us control exactly what we expose publicly
 * - We can evolve internal gRPC contracts without breaking REST API
 */
public class GatewayDtos {

    // ─────────────────────────────────────────────
    //  User DTOs
    // ─────────────────────────────────────────────

    /** Request body for POST /api/users */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateUserRequestold {
        public String name;
        public String email;
    }

    /** Request body for PUT /api/users/{id} */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateUserRequestold {
        public String name;
        public String email;
    }

    /** Response for user operations */
    public static class UserDto {
        public String id;
        public String name;
        public String email;

        @JsonProperty("created_at")
        public String createdAt;

        @JsonProperty("updated_at")
        public String updatedAt;
    }

    /** Wrapper for single user response */
    public static class UserResponse {
        public UserDto user;
        public String message;

        public UserResponse(UserDto user, String message) {
            this.user    = user;
            this.message = message;
        }
    }

    /** Wrapper for list of users */
    public static class UsersResponse {
        public java.util.List<UserDto> users;
        public int total;

        public UsersResponse(java.util.List<UserDto> users, int total) {
            this.users = users;
            this.total = total;
        }
    }

    // ─────────────────────────────────────────────
    //  Order DTOs
    // ─────────────────────────────────────────────

    /** Request body for POST /api/orders */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateOrderRequest {
        @JsonProperty("userId")
        public String userId;

        @JsonProperty("productName")
        public String productName;

        public int quantity;
        public double price;
    }

    /** Request body for PUT /api/orders/{id}/status */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateOrderStatusRequest {
        public String status;  // "PENDING" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED"
    }

    /** Response for order operations */
    public static class OrderDto {
        public String id;

        @JsonProperty("user_id")
        public String userId;

        @JsonProperty("product_name")
        public String productName;

        public int quantity;
        public double price;
        public String status;

        @JsonProperty("created_at")
        public String createdAt;

        @JsonProperty("updated_at")
        public String updatedAt;
    }

    /** Wrapper for single order response */
    public static class OrderResponse {
        public OrderDto order;
        public String message;

        public OrderResponse(OrderDto order, String message) {
            this.order   = order;
            this.message = message;
        }
    }

    /** Wrapper for list of orders */
    public static class OrdersResponse {
        public java.util.List<OrderDto> orders;
        public int total;

        public OrdersResponse(java.util.List<OrderDto> orders, int total) {
            this.orders = orders;
            this.total  = total;
        }
    }

    // ─────────────────────────────────────────────
    //  Generic
    // ─────────────────────────────────────────────

    public static class DeleteResponse {
        public boolean success;
        public String message;

        public DeleteResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class ErrorResponse {
        public String error;
        public String message;
        public int    status;

        public ErrorResponse(String error, String message, int status) {
            this.error   = error;
            this.message = message;
            this.status  = status;
        }
    }
}
