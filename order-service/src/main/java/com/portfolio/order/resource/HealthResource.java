package com.portfolio.order.resource;

import com.portfolio.order.service.OrderStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    OrderStore orderStore;

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
                "status",     "UP",
                "service",    "order-service",
                "timestamp",  Instant.now().toString(),
                "orderCount", orderStore.count()
        )).build();
    }

    @GET
    @Path("/info")
    public Response info() {
        return Response.ok(Map.of(
                "service",     "order-service",
                "version",     "1.0.0",
                "description", "Order management microservice (gRPC)",
                "grpcPort",    9001,
                "httpPort",    8082
        )).build();
    }
}
