package com.portfolio.user.grpc;

import com.portfolio.user.service.UserStore;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

/**
 * gRPC Service Implementation for UserService.
 *
 * This class implements the server-side logic for each RPC method defined
 * in user.proto. Quarkus generates the base class (UserServiceGrpc.UserServiceImplBase)
 * from the .proto file at compile time.
 *
 * Key patterns used:
 * - StreamObserver for async response handling (gRPC standard)
 * - Status codes for proper error propagation
 * - Dependency injection via @Inject
 */
@GrpcService
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger LOG = Logger.getLogger(UserServiceGrpcImpl.class);

    @Inject
    UserStore userStore;

    // ─────────────────────────────────────────────────────────────────
    //  RPC: CreateUser
    //  Creates a new user and returns it.
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void createUser(CreateUserRequest request,
                           StreamObserver<UserResponse> responseObserver) {
        LOG.infof("gRPC CreateUser called — name=%s, email=%s",
                request.getName(), request.getEmail());

        try {
            User created = userStore.create(request.getName(), request.getEmail());

            UserResponse response = UserResponse.newBuilder()
                    .setUser(created)
                    .setMessage("User created successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            LOG.warnf("CreateUser validation error: %s", e.getMessage());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(e.getMessage())
                            .asRuntimeException()
            );
        } catch (Exception e) {
            LOG.errorf(e, "CreateUser unexpected error");
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error")
                            .asRuntimeException()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: GetUser
    //  Retrieves a single user by ID.
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void getUser(GetUserRequest request,
                        StreamObserver<UserResponse> responseObserver) {
        LOG.infof("gRPC GetUser called — id=%s", request.getId());

        if (request.getId().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("User ID cannot be empty")
                            .asRuntimeException()
            );
            return;
        }

        Optional<User> userOpt = userStore.findById(request.getId());

        if (userOpt.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User not found with ID: " + request.getId())
                            .asRuntimeException()
            );
            return;
        }

        UserResponse response = UserResponse.newBuilder()
                .setUser(userOpt.get())
                .setMessage("User retrieved successfully")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: ListUsers
    //  Returns a paginated list of all users.
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void listUsers(ListUsersRequest request,
                          StreamObserver<ListUsersResponse> responseObserver) {
        LOG.infof("gRPC ListUsers called — page=%d, pageSize=%d",
                request.getPage(), request.getPageSize());

        try {
            List<User> users = userStore.findAll(request.getPage(), request.getPageSize());
            int total = userStore.count();

            ListUsersResponse response = ListUsersResponse.newBuilder()
                    .addAllUsers(users)
                    .setTotal(total)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            LOG.errorf(e, "ListUsers error");
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Failed to list users").asRuntimeException()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: UpdateUser
    //  Updates an existing user's name or email.
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void updateUser(UpdateUserRequest request,
                           StreamObserver<UserResponse> responseObserver) {
        LOG.infof("gRPC UpdateUser called — id=%s", request.getId());

        try {
            Optional<User> updated = userStore.update(
                    request.getId(), request.getName(), request.getEmail()
            );

            if (updated.isEmpty()) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("User not found with ID: " + request.getId())
                                .asRuntimeException()
                );
                return;
            }

            UserResponse response = UserResponse.newBuilder()
                    .setUser(updated.get())
                    .setMessage("User updated successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: DeleteUser
    //  Removes a user from the store.
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void deleteUser(DeleteUserRequest request,
                           StreamObserver<DeleteUserResponse> responseObserver) {
        LOG.infof("gRPC DeleteUser called — id=%s", request.getId());

        boolean deleted = userStore.delete(request.getId());

        DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setSuccess(deleted)
                .setMessage(deleted
                        ? "User deleted successfully"
                        : "User not found with ID: " + request.getId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
