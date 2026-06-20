package com.portfolio.order.grpc;

import com.portfolio.order.service.OrderStore;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

/**
 * gRPC Service Implementation for OrderService.
 *
 * Implements all RPC methods defined in order.proto.
 * Each method follows the pattern:
 *   1. Validate input
 *   2. Call business logic (OrderStore)
 *   3. Build Protobuf response
 *   4. Call responseObserver.onNext() + onCompleted()
 *   5. On error: call responseObserver.onError() with proper gRPC Status
 */
@GrpcService
public class OrderServiceGrpcImpl extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger LOG = Logger.getLogger(OrderServiceGrpcImpl.class);

    @Inject
    OrderStore orderStore;

    // ─────────────────────────────────────────────────────────────────
    //  RPC: CreateOrder
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void createOrder(CreateOrderRequest request,
                            StreamObserver<OrderResponse> responseObserver) {
        LOG.infof("gRPC CreateOrder — userId=%s, product=%s, qty=%d, price=%.2f",
                request.getUserId(), request.getProductName(),
                request.getQuantity(), request.getPrice());

        try {
            Order order = orderStore.create(
                    request.getUserId(),
                    request.getProductName(),
                    request.getQuantity(),
                    request.getPrice()
            );

            responseObserver.onNext(OrderResponse.newBuilder()
                    .setOrder(order)
                    .setMessage("Order created successfully")
                    .build());
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            LOG.errorf(e, "CreateOrder error");
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal server error").asRuntimeException()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: GetOrder
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void getOrder(GetOrderRequest request,
                         StreamObserver<OrderResponse> responseObserver) {
        LOG.infof("gRPC GetOrder — id=%s", request.getId());

        Optional<Order> orderOpt = orderStore.findById(request.getId());

        if (orderOpt.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Order not found: " + request.getId())
                            .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(OrderResponse.newBuilder()
                .setOrder(orderOpt.get())
                .setMessage("Order retrieved successfully")
                .build());
        responseObserver.onCompleted();
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: ListOrdersByUser
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void listOrdersByUser(ListOrdersByUserRequest request,
                                 StreamObserver<ListOrdersResponse> responseObserver) {
        LOG.infof("gRPC ListOrdersByUser — userId=%s", request.getUserId());

        if (request.getUserId().isBlank()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("User ID is required")
                            .asRuntimeException()
            );
            return;
        }

        List<Order> orders = orderStore.findByUserId(request.getUserId());

        responseObserver.onNext(ListOrdersResponse.newBuilder()
                .addAllOrders(orders)
                .setTotal(orders.size())
                .build());
        responseObserver.onCompleted();
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: UpdateOrderStatus
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void updateOrderStatus(UpdateOrderStatusRequest request,
                                  StreamObserver<OrderResponse> responseObserver) {
        LOG.infof("gRPC UpdateOrderStatus — id=%s, status=%s",
                request.getId(), request.getStatus());

        try {
            Optional<Order> updated = orderStore.updateStatus(request.getId(), request.getStatus());

            if (updated.isEmpty()) {
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("Order not found: " + request.getId())
                                .asRuntimeException()
                );
                return;
            }

            responseObserver.onNext(OrderResponse.newBuilder()
                    .setOrder(updated.get())
                    .setMessage("Order status updated to " + request.getStatus())
                    .build());
            responseObserver.onCompleted();

        } catch (IllegalStateException e) {
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  RPC: DeleteOrder
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void deleteOrder(DeleteOrderRequest request,
                            StreamObserver<DeleteOrderResponse> responseObserver) {
        LOG.infof("gRPC DeleteOrder — id=%s", request.getId());

        boolean deleted = orderStore.delete(request.getId());

        responseObserver.onNext(DeleteOrderResponse.newBuilder()
                .setSuccess(deleted)
                .setMessage(deleted
                        ? "Order deleted successfully"
                        : "Order not found: " + request.getId())
                .build());
        responseObserver.onCompleted();
    }
}
