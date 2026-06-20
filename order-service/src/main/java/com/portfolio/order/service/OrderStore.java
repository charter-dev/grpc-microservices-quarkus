package com.portfolio.order.service;

import com.portfolio.order.grpc.Order;
import com.portfolio.order.grpc.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory order store — simulates a database layer.
 *
 * Thread-safe implementation using ConcurrentHashMap.
 * In production, replace with Quarkus Panache + PostgreSQL or MongoDB.
 */
@ApplicationScoped
public class OrderStore {

    private final Map<String, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public OrderStore() {
        seedData();
    }

    // ─────────────────────────────────────────────
    //  CRUD Operations
    // ─────────────────────────────────────────────

    public Order create(String userId, String productName, int quantity, double price) {
        validate(userId, productName, quantity, price);

        String id  = String.valueOf(idCounter.getAndIncrement());
        String now = Instant.now().toString();

        Order order = Order.newBuilder()
                .setId(id)
                .setUserId(userId)
                .setProductName(productName.trim())
                .setQuantity(quantity)
                .setPrice(price)
                .setStatus(OrderStatus.PENDING)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();

        store.put(id, order);
        return order;
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Order> findByUserId(String userId) {
        return store.values().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted((a, b) -> Long.compare(
                        Long.parseLong(b.getId()), Long.parseLong(a.getId())
                ))
                .toList();
    }

    public List<Order> findAll() {
        return store.values().stream()
                .sorted((a, b) -> Long.compare(
                        Long.parseLong(b.getId()), Long.parseLong(a.getId())
                ))
                .toList();
    }

    public int count() {
        return store.size();
    }

    public Optional<Order> updateStatus(String id, OrderStatus newStatus) {
        Order existing = store.get(id);
        if (existing == null) return Optional.empty();

        // Business rule: cannot change status of a DELIVERED or CANCELLED order
        if (existing.getStatus() == OrderStatus.DELIVERED ||
                existing.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot update status of a " + existing.getStatus() + " order"
            );
        }

        Order updated = existing.toBuilder()
                .setStatus(newStatus)
                .setUpdatedAt(Instant.now().toString())
                .build();

        store.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    // ─────────────────────────────────────────────
    //  Validation
    // ─────────────────────────────────────────────

    private void validate(String userId, String productName, int quantity, double price) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    // ─────────────────────────────────────────────
    //  Seed Data
    // ─────────────────────────────────────────────

    private void seedData() {
        String now = Instant.now().toString();

        // User 1 orders
        String id1 = String.valueOf(idCounter.getAndIncrement());
        store.put(id1, Order.newBuilder()
                .setId(id1).setUserId("1")
                .setProductName("Laptop ASUS ROG").setQuantity(1).setPrice(15000000)
                .setStatus(OrderStatus.CONFIRMED).setCreatedAt(now).setUpdatedAt(now)
                .build());

        String id2 = String.valueOf(idCounter.getAndIncrement());
        store.put(id2, Order.newBuilder()
                .setId(id2).setUserId("1")
                .setProductName("Mouse Logitech MX3").setQuantity(2).setPrice(800000)
                .setStatus(OrderStatus.SHIPPED).setCreatedAt(now).setUpdatedAt(now)
                .build());

        // User 2 orders
        String id3 = String.valueOf(idCounter.getAndIncrement());
        store.put(id3, Order.newBuilder()
                .setId(id3).setUserId("2")
                .setProductName("Mechanical Keyboard").setQuantity(1).setPrice(1200000)
                .setStatus(OrderStatus.PENDING).setCreatedAt(now).setUpdatedAt(now)
                .build());
    }
}
