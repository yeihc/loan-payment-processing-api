package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure Adapter: Spring Data JPA implementation for the User entity.
 *
 * <p>This interface leverages Spring Data's Query Method mechanism to generate
 * SQL queries automatically based on method names. It acts as the direct
 * communication link with the relational database.</p>
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Derived Queries:</b> Methods like 'findByEmail' are automatically
 * implemented by Spring Data, reducing boilerplate code.</li>
 * <li><b>Pre-emptive Validations:</b> The 'existsBy...' methods are optimized
 * to perform a 'SELECT 1' style query, which is more efficient than loading
 * the entire entity just to check for existence.</li>
 * </ul>
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user based on their unique email address.
     * @param email Registered email to search.
     * @return An Optional containing the User if present.
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves a user based on their official government identifier (DNI, SSN, TaxId).
     * @param taxId The unique tax identifier.
     * @return An Optional containing the User if present.
     */
    Optional<User> findByTaxId(String taxId);

    /**
     * Checks if a Tax ID is already registered in the system.
     * Crucial for preventing duplicate legal identities.
     * @param taxId Tax ID to verify.
     * @return true if the identifier already exists.
     */
    boolean existsByTaxId(String taxId);

    /**
     * Checks if an email is already associated with an account.
     * * Useful during the onboarding process to avoid unique constraint violations
     * at the database level.
     * @param email Email address to verify.
     * @return true if the email is already in use.
     */
    boolean existsByEmail(String email);
}