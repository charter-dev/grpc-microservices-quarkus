package com.portfolio.gateway.resource;

import com.portfolio.gateway.dto.GatewayDtos.CreateOrderRequest;
import com.portfolio.gateway.dto.GatewayDtos.UpdateOrderStatusRequest;
import com.portfolio.gateway.dto.GatewayDtos.OrderDto;
import com.portfolio.gateway.dto.GatewayDtos.OrdersResponse;
import com.portfolio.gateway.dto.GatewayDtos.ErrorResponse;
import com.portfolio.gateway.dto.GatewayDtos.DeleteResponse;

import com.portfolio.order.grpc.Order;
import com.portfolio.order.grpc.OrderStatus;
import com.portfolio.order.grpc.OrderResponse;
import com.portfolio.order.grpc.ListOrdersResponse;
import com.portfolio.order.grpc.ListOrdersByUserRequest;
import com.portfolio.order.grpc.GetOrderRequest;
import com.portfolio.order.grpc.DeleteOrderRequest;
import com.portfolio.order.grpc.DeleteOrderResponse;
import com.portfolio.order.grpc.OrderServiceGrpc;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * REST Resource for Order operations.
 *
 * Translates REST HTTP calls → gRPC calls to order-service.
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);

    @GrpcClient("order-service")
    OrderServiceGrpc.OrderServiceBlockingStub orderServiceClient;

    // ─────────────────────────────────────────────────────────────────
    //  POST /api/orders  →  gRPC CreateOrder
    // ─────────────────────────────────────────────────────────────────

    @POST
    public Response createOrder(CreateOrderRequest body) {
        LOG.infof("REST POST /api/orders — userId=%s, product=%s",
                body.userId, body.productName);

        try {
            OrderResponse grpcResponse = orderServiceClient.createOrder(
                    com.portfolio.order.grpc.CreateOrderRequest.newBuilder()
                            .setUserId(nullSafe(body.userId))
                            .setProductName(nullSafe(body.productName))
                            .setQuantity(body.quantity)
                            .setPrice(body.price)
                            .build()
            );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(new com.portfolio.gateway.dto.GatewayDtos.OrderResponse(
                            toDto(grpcResponse.getOrder()),
                            grpcResponse.getMessage()
                    ))
                    .build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /api/orders  →  all orders (via ListOrdersByUser with empty userId = all)
    // ─────────────────────────────────────────────────────────────────

    @GET
    public Response listOrders() {
        LOG.info("REST GET /api/orders");

        try {
            // List all orders by fetching with a wildcard marker
            // In production, you'd add a ListAllOrders RPC
            ListOrdersResponse grpcResponse = orderServiceClient.listOrdersByUser(
                    ListOrdersByUserRequest.newBuilder()
                            .setUserId("*")  // convention: "*" = return all
                            .build()
            );

            List<OrderDto> orders = grpcResponse.getOrdersList().stream()
                    .map(this::toDto)
                    .toList();

            return Response.ok(new OrdersResponse(orders, grpcResponse.getTotal())).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /api/orders/{id}  →  gRPC GetOrder
    // ─────────────────────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    public Response getOrder(@PathParam("id") String id) {
        LOG.infof("REST GET /api/orders/%s", id);

        try {
            OrderResponse grpcResponse = orderServiceClient.getOrder(
                    GetOrderRequest.newBuilder().setId(id).build()
            );

            return Response.ok(new com.portfolio.gateway.dto.GatewayDtos.OrderResponse(
                    toDto(grpcResponse.getOrder()),
                    grpcResponse.getMessage()
            )).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  GET /api/orders/user/{userId}  →  gRPC ListOrdersByUser
    // ─────────────────────────────────────────────────────────────────

    @GET
    @Path("/user/{userId}")
    public Response getOrdersByUser(@PathParam("userId") String userId) {
        LOG.infof("REST GET /api/orders/user/%s", userId);

        try {
            ListOrdersResponse grpcResponse = orderServiceClient.listOrdersByUser(
                    ListOrdersByUserRequest.newBuilder().setUserId(userId).build()
            );

            List<OrderDto> orders = grpcResponse.getOrdersList().stream()
                    .map(this::toDto)
                    .toList();

            return Response.ok(new OrdersResponse(orders, grpcResponse.getTotal())).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  PUT /api/orders/{id}/status  →  gRPC UpdateOrderStatus
    // ─────────────────────────────────────────────────────────────────

    @PUT
    @Path("/{id}/status")
    public Response updateOrderStatus(
            @PathParam("id") String id,
            com.portfolio.gateway.dto.GatewayDtos.UpdateOrderStatusRequest body
    ) {
        LOG.infof("REST PUT /api/orders/%s/status — status=%s", id, body.status);

        try {
            // Parse status string to Protobuf enum
            OrderStatus status;
            try {
                status = OrderStatus.valueOf(body.status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(400)
                        .entity(new ErrorResponse(
                                "BAD_REQUEST",
                                "Invalid status. Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED",
                                400
                        ))
                        .build();
            }

            // 🚨 INI BAGIAN YANG HARUS DIPASTIKAN GRPC (BUKAN DTO)
            com.portfolio.order.grpc.UpdateOrderStatusRequest grpcRequest =
                    com.portfolio.order.grpc.UpdateOrderStatusRequest.newBuilder()
                            .setId(id)
                            .setStatus(status)
                            .build();

            OrderResponse grpcResponse =
                    orderServiceClient.updateOrderStatus(grpcRequest);

            return Response.ok(new com.portfolio.gateway.dto.GatewayDtos.OrderResponse(
                    toDto(grpcResponse.getOrder()),
                    grpcResponse.getMessage()
            )).build();

        } catch (StatusRuntimeException e) {
            return handleGrpcError(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DELETE /api/orders/{id}  →  gRPC DeleteOrder
    // ─────────────────────────────────────────────────────────────────

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") String id) {
        LOG.infof("REST DELETE /api/orders/%s", id);

        try {
            DeleteOrderResponse grpcResponse = orderServiceClient.deleteOrder(
                    DeleteOrderRequest.newBuilder().setId(id).build()
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

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.id          = order.getId();
        dto.userId      = order.getUserId();
        dto.productName = order.getProductName();
        dto.quantity    = order.getQuantity();
        dto.price       = order.getPrice();
        dto.status      = order.getStatus().name();
        dto.createdAt   = order.getCreatedAt();
        dto.updatedAt   = order.getUpdatedAt();
        return dto;
    }

    private Response handleGrpcError(StatusRuntimeException e) {
        String description = e.getStatus().getDescription();
        LOG.warnf("gRPC error: %s — %s", e.getStatus().getCode(), description);

        return switch (e.getStatus().getCode()) {
            case NOT_FOUND           -> Response.status(404)
                    .entity(new ErrorResponse("NOT_FOUND", description, 404)).build();
            case INVALID_ARGUMENT    -> Response.status(400)
                    .entity(new ErrorResponse("BAD_REQUEST", description, 400)).build();
            case FAILED_PRECONDITION -> Response.status(422)
                    .entity(new ErrorResponse("UNPROCESSABLE", description, 422)).build();
            default                  -> Response.status(500)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", 500)).build();
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
