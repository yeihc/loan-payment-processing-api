package com.yeihc.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Entity representing the legal owner of bank accounts.
 * * DESIGN DECISIONS:
 * 1. Independent Aggregate Root: User is a top-level entity. Accounts refer to it
 * by its ID, maintaining a clear boundary between identity and financial assets.
 * 2. Strict Identity: Enforces unique constraints on critical identifiers (email, taxId)
 * to prevent duplicate customer profiles in the system.
 * 3. Minimalist Domain: Focuses only on identity data required for basic banking
 * operations, following the Principle of Least Privilege.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "taxId")
})
@Getter
public class User {

    /**
     * Unique internal identifier for the user.
     */
    @Id
    private UUID id;

    /**
     * Legal full name of the customer.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Electronic mail address. Used for notifications and authentication.
     * Must be unique across the platform.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Official government tax identifier (e.g., DNI, SSN, CPF).
     * This is the primary key for legal and regulatory reporting.
     */
    @Column(nullable = false, unique = true)
    private String taxId;

    protected User() {
        // Required for JPA proxies and reflection
    }

    /**
     * Standard constructor for creating a new User aggregate.
     * * @param id      The unique UUID for the user.
     * @param name    Full legal name.
     * @param email   Unique valid email address.
     * @param taxId   Unique government identification number.
     * @throws NullPointerException if any required field is missing.
     */
    public User(UUID id, String name, String email, String taxId) {
        this.id = Objects.requireNonNull(id, "User ID is required");
        this.name = Objects.requireNonNull(name, "Name is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.taxId = Objects.requireNonNull(taxId, "Tax ID is required");

        validateEmail(email);
    }

    private void validateEmail(String email) {
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}