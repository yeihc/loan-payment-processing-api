package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.User;
import com.yeihc.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary Adapter: Implementation of the UserRepository Port using JPA.
 * * <p>This class fulfills the contract defined by the Domain layer, translating
 * domain requests into specific Spring Data JPA calls. It acts as a protective
 * shield for the Domain, ensuring it remains agnostic of the persistence technology.</p>
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Dependency Inversion:</b> This class implements a Domain interface but resides
 * in the Infrastructure layer, satisfying the Dependency Inversion Principle (DIP).</li>
 * <li><b>Separation of Concerns:</b> By delegating to 'SpringDataUserRepository', we
 * separate the mapping logic from the high-level repository contract.</li>
 * <li><b>Transaction Management:</b> While the Use Case marks the transaction boundary,
 * this adapter ensures data is correctly flushed to the underlying relational store.</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    /**
     * Persists the User aggregate into the database.
     * @param user The aggregate root to be saved.
     */
    @Override
    public void save(User user) {
        springDataUserRepository.save(user);
    }

    /**
     * Finds a user by their unique primary key.
     * @param id UUID of the user.
     * @return Optional containing the user if found.
     */
    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id);
    }

    /**
     * Look up a user by their tax identification number.
     * Used for compliance and identity verification.
     */
    @Override
    public Optional<User> findByTaxId(String taxId) {
        return springDataUserRepository.findByTaxId(taxId);
    }

    /**
     * Look up a user by their unique email address.
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }

    /**
     * Checks for the existence of a Tax ID.
     * Implementation of the domain's requirement for uniqueness validation.
     */
    @Override
    public boolean existsByTaxId(String taxId) {
        return springDataUserRepository.existsByTaxId(taxId);
    }

    /**
     * Checks for the existence of an email address.
     * Added to the port to support early validation in the application layer.
     */
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }
}