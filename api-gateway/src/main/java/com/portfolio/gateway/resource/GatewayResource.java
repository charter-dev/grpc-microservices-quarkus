package com.portfolio.gateway.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

/**
 * API Gateway health and discovery endpoint.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class GatewayResource {

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
                "status",    "UP",
                "service",   "api-gateway",
                "timestamp", Instant.now().toString()
        )).build();
    }

    @GET
    @Path("/api")
    public Response apiInfo() {
        return Response.ok(Map.of(
                "service",     "api-gateway",
                "version",     "1.0.0",
                "description", "REST API Gateway — translates HTTP to gRPC",
                "upstreams",   Map.of(
                        "user-service",  "grpc://user-service:9000",
                        "order-service", "grpc://order-service:9001"
                ),
                "endpoints", Map.of(
                        "users",  new String[]{
                                "POST   /api/users",
                                "GET    /api/users",
                                "GET    /api/users/{id}",
                                "PUT    /api/users/{id}",
                                "DELETE /api/users/{id}"
                        },
                        "orders", new String[]{
                                "POST   /api/orders",
                                "GET    /api/orders",
                                "GET    /api/orders/{id}",
                                "GET    /api/orders/user/{userId}",
                                "PUT    /api/orders/{id}/status",
                                "DELETE /api/orders/{id}"
                        }
                )
        )).build();
    }
}
