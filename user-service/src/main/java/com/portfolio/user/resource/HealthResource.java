package com.portfolio.user.resource;

import com.portfolio.user.service.UserStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;

/**
 * REST resource for service health and info.
 *
 * Note: The main user operations are exposed via gRPC.
 * This REST endpoint is used for:
 * - Health checks (liveness/readiness probes in K8s)
 * - Service metadata
 * - Monitoring dashboards
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    UserStore userStore;

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
                "status",    "UP",
                "service",   "user-service",
                "timestamp", Instant.now().toString(),
                "userCount", userStore.count()
        )).build();
    }

    @GET
    @Path("/info")
    public Response info() {
        return Response.ok(Map.of(
                "service",     "user-service",
                "version",     "1.0.0",
                "description", "User management microservice (gRPC)",
                "grpcPort",    9000,
                "httpPort",    8081,
                "endpoints",   new String[]{
                        "gRPC: CreateUser",
                        "gRPC: GetUser",
                        "gRPC: ListUsers",
                        "gRPC: UpdateUser",
                        "gRPC: DeleteUser"
                }
        )).build();
    }
}
