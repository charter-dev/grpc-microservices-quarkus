package com.portfolio.user.service;

import com.portfolio.user.grpc.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory user store — simulates a database layer.
 *
 * In a production service this would be replaced with:
 * - Quarkus Panache + PostgreSQL (recommended)
 * - MongoDB with Panache reactive
 * - Redis for caching
 */
@ApplicationScoped
public class UserStore {

    // Thread-safe in-memory map (simulates DB table)
    private final Map<String, User> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public UserStore() {
        // Seed initial data for demo purposes
        seedData();
    }

    // ─────────────────────────────────────────────
    //  CRUD Operations
    // ─────────────────────────────────────────────

    public User create(String name, String email) {
        validateEmail(email);
        validateName(name);

        // Check email uniqueness
        boolean emailExists = store.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        if (emailExists) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        String id = String.valueOf(idCounter.getAndIncrement());
        String now = Instant.now().toString();

        User user = User.newBuilder()
                .setId(id)
                .setName(name.trim())
                .setEmail(email.trim().toLowerCase())
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();

        store.put(id, user);
        return user;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<User> findAll(int page, int pageSize) {
        // Default pagination values
        int effectivePage     = Math.max(0, page);
        int effectivePageSize = (pageSize <= 0 || pageSize > 100) ? 10 : pageSize;

        return store.values().stream()
                .sorted((a, b) -> Long.compare(
                        Long.parseLong(a.getId()),
                        Long.parseLong(b.getId())
                ))
                .skip((long) effectivePage * effectivePageSize)
                .limit(effectivePageSize)
                .toList();
    }

    public int count() {
        return store.size();
    }

    public Optional<User> update(String id, String name, String email) {
        User existing = store.get(id);
        if (existing == null) {
            return Optional.empty();
        }

        User.Builder builder = existing.toBuilder()
                .setUpdatedAt(Instant.now().toString());

        if (name != null && !name.isBlank()) {
            validateName(name);
            builder.setName(name.trim());
        }
        if (email != null && !email.isBlank()) {
            validateEmail(email);
            builder.setEmail(email.trim().toLowerCase());
        }

        User updated = builder.build();
        store.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    // ─────────────────────────────────────────────
    //  Validation
    // ─────────────────────────────────────────────

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Name must be at least 2 characters");
        }
    }

    // ─────────────────────────────────────────────
    //  Seed Data
    // ─────────────────────────────────────────────

    private void seedData() {
        String now = Instant.now().toString();

        List<String[]> seeds = List.of(
                new String[]{"Budi Santoso",  "budi.santoso@example.com"},
                new String[]{"Siti Rahayu",   "siti.rahayu@example.com"},
                new String[]{"Ahmad Fauzi",   "ahmad.fauzi@example.com"}
        );

        for (String[] s : seeds) {
            String id = String.valueOf(idCounter.getAndIncrement());
            store.put(id, User.newBuilder()
                    .setId(id)
                    .setName(s[0])
                    .setEmail(s[1])
                    .setCreatedAt(now)
                    .setUpdatedAt(now)
                    .build());
        }
    }
}
