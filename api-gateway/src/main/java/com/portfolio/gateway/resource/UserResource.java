package com.portfolio.gateway.resource;

//gRPC
import com.portfolio.user.grpc.UserServiceGrpc;
import com.portfolio.user.grpc.User;
import com.portfolio.user.grpc.CreateUserRequest;
import com.portfolio.user.grpc.UpdateUserRequest;
import com.portfolio.user.grpc.DeleteUserRequest;
import com.portfolio.user.grpc.GetUserRequest;
import com.portfolio.user.grpc.ListUsersRequest;
import com.portfolio.user.grpc.UserResponse;
import com.portfolio.user.grpc.ListUsersResponse;
import com.portfolio.user.grpc.DeleteUserResponse;

//REST DTO
import com.portfolio.gateway.dto.GatewayDtos.UserDto;
import com.portfolio.gateway.dto.GatewayDtos.UsersResponse;
import com.portfolio.gateway.dto.GatewayDtos.DeleteResponse;
import com.portfolio.gateway.dto.GatewayDtos.ErrorResponse;
import com.portfolio.gateway.dto.GatewayDtos.CreateUserRequestold;
import com.portfolio.gateway.dto.GatewayDtos.UpdateUserRequestold;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST Resource for User operations.
 *
 * This is the "translator" between HTTP/REST (what clients send)
 * and gRPC (what the user-service understands).
 *
 * Flow for each request:
 *   HTTP Request → Parse JSON → Build Protobuf message
 *       → gRPC call to user-service → Parse Protobuf response
 *       → Build JSON DTO → HTTP Response
 *
 * Error handling:
 *   gRPC StatusRuntimeException → mapped to appropriate HTTP status codes
 *   (NOT_FOUND → 404, INVALID_ARGUMENT → 400, INTERNAL → 500)
 */
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class);

    /**
     * Inject a blocking gRPC stub for the user-service.
     *
     * @GrpcClient("user-service") tells Quarkus to:
     *   1. Look up the "user-service" channel config in application.properties
     *   2. Create a ManagedChannel to that host:port
     *   3. Inject a blocking stub (UserServiceGrpc.UserServiceBlockingStub)
     *
     * Blocking stub = synchronous call (thread blocks until response arrives).
     * For reactive/non-blocking: use MutinyUserServiceGrpc instead.
     */
    @GrpcClient("user-service")
    UserServiceGrpc.UserServiceBlockingStub userServiceClient;

    // ─────────────────────────────────────────────────────────────────
    //  POST /api/users  →  gRPC CreateUser
    // ─────────────────────────────────────────────────────────────────

    @POST
    public Response createUser(CreateUserRequestold body) {
        LOG.infof("REST POST /api/users — name=%s, email=%s", body.name, body.email);

        try {
            // Build Protobuf request
            var grpcRequest = com.portfolio.user.grpc.CreateUserRequest.newBuilder()
                    .setName(nullSafe(body.name))
                    .setEmail(nullSafe(body.email))
                    .build();

            // Make gRPC call (blocking)
            UserResponse grpcResponse = userServiceClient.createUser(grpcRequest);

            // Convert Protobuf → DTO → JSON
            return Response
                    .status(Response.Status.CREATED)
                    .entity(new com.portfolio.gateway.dto.GatewayDtos.UserResponse(
                            toDto(grpcResponse.getUser()),
                            grpcResponse.getMessage()
                    ))
                    .build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /api/users  →  gRPC ListUsers
    // ─────────────────────────────────────────────────────────────────

    @GET
    public Response listUsers(
            @QueryParam("page")     @DefaultValue("0")  int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize
    ) {
        LOG.infof("REST GET /api/users — page=%d, pageSize=%d", page, pageSize);

        try {
            ListUsersResponse grpcResponse = userServiceClient.listUsers(
                    ListUsersRequest.newBuilder()
                            .setPage(page)
                            .setPageSize(pageSize)
                            .build()
            );

            List<UserDto> users = grpcResponse.getUsersList().stream()
                    .map(this::toDto)
                    .toList();

            return Response.ok(new UsersResponse(users, grpcResponse.getTotal())).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /api/users/{id}  →  gRPC GetUser
    // ─────────────────────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") String id) {
        LOG.infof("REST GET /api/users/%s", id);

        try {
            UserResponse grpcResponse = userServiceClient.getUser(
                    GetUserRequest.newBuilder().setId(id).build()
            );

            return Response.ok(new com.portfolio.gateway.dto.GatewayDtos.UserResponse(
                    toDto(grpcResponse.getUser()),
                    grpcResponse.getMessage()
            )).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  PUT /api/users/{id}  →  gRPC UpdateUser
    // ─────────────────────────────────────────────────────────────────

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, UpdateUserRequestold body) {
        LOG.infof("REST PUT /api/users/%s", id);

        try {
            UserResponse grpcResponse = userServiceClient.updateUser(
                    com.portfolio.user.grpc.UpdateUserRequest.newBuilder()
                            .setId(id)
                            .setName(nullSafe(body.name))
                            .setEmail(nullSafe(body.email))
                            .build()
            );

            return Response.ok(new com.portfolio.gateway.dto.GatewayDtos.UserResponse(
                    toDto(grpcResponse.getUser()),
                    grpcResponse.getMessage()
            )).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DELETE /api/users/{id}  →  gRPC DeleteUser
    // ─────────────────────────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        LOG.infof("REST DELETE /api/users/%s", id);

        try {
            DeleteUserResponse grpcResponse = userServiceClient.deleteUser(
                    DeleteUserRequest.newBuilder().setId(id).build()
            );

            return Response.ok(new DeleteResponse(
                    grpcResponse.getSuccess(),
                    grpcResponse.getMessage()
            )).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────

    /** Convert Protobuf User → JSON-friendly DTO */
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.id        = user.getId();
        dto.name      = user.getName();
        dto.email     = user.getEmail();
        dto.createdAt = user.getCreatedAt();
        dto.updatedAt = user.getUpdatedAt();
        return dto;
    }

    /**
     * Map gRPC status codes to HTTP status codes.
     *
     * gRPC Status  → HTTP Status
     * NOT_FOUND    → 404
     * INVALID_ARG  → 400
     * ALREADY_EX.  → 409
     * INTERNAL     → 500
     */
    private Response handleGrpcError(StatusRuntimeException e) {
        String description = e.getStatus().getDescription();
        LOG.warnf("gRPC error: %s — %s", e.getStatus().getCode(), description);

        return switch (e.getStatus().getCode()) {
            case NOT_FOUND         -> Response.status(404)
                    .entity(new ErrorResponse("NOT_FOUND", description, 404)).build();
            case INVALID_ARGUMENT  -> Response.status(400)
                    .entity(new ErrorResponse("BAD_REQUEST", description, 400)).build();
            case ALREADY_EXISTS    -> Response.status(409)
                    .entity(new ErrorResponse("CONFLICT", description, 409)).build();
            case UNAUTHENTICATED   -> Response.status(401)
                    .entity(new ErrorResponse("UNAUTHORIZED", description, 401)).build();
            case PERMISSION_DENIED -> Response.status(403)
                    .entity(new ErrorResponse("FORBIDDEN", description, 403)).build();
            default                -> Response.status(500)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", 500)).build();
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
