package com.yeihc.domain.repository;

import com.yeihc.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (Interface) for User aggregate persistence and retrieval.
 *
 * <p>DESIGN DECISIONS:</p>
 * <ul>
 * <li><b>Identity Access:</b> Provides multiple lookup strategies (ID, Email, TaxId)
 * to support different business workflows like onboarding and authentication.</li>
 * <li><b>Domain Purity:</b> This interface belongs to the Domain layer, ensuring
 * that business logic is never coupled to specific database technologies (SQL/NoSQL).</li>
 * <li><b>Integrity:</b> Used by Account services to verify that an account owner
 * exists and is legally valid before account creation.</li>
 * </ul>
 */
public interface UserRepository {

    /**
     * Persists a User aggregate.
     * @param user The user entity to save or update.
     */
    void save(User user);

    /**
     * Finds a user by their unique internal identifier.
     * @param id Unique UUID of the user.
     * @return An Optional containing the user if found.
     */
    Optional<User> findById(UUID id);

    /**
     * Retrieves a user by their official government tax identifier.
     * Crucial for preventing duplicate registrations of the same legal person.
     * @param taxId DNI, SSN, CPF, etc.
     * @return An Optional containing the user.
     */
    Optional<User> findByTaxId(String taxId);

    /**
     * Finds a user by their unique email address.
     * Primarily used for authentication and communication workflows.
     * @param email The registered email address.
     * @return An Optional containing the user.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given tax identifier.
     * @param taxId The tax ID to check.
     * @return true if already registered.
     */
    boolean existsByTaxId(String taxId);
}