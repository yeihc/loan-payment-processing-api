package com.yeihc.infrastructure.persistence.jpa;

import com.yeihc.domain.model.Account;
import com.yeihc.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary Adapter: Implementation of the AccountRepository Port using Spring Data JPA.
 *
 * <p>This class acts as the concrete implementation of the domain's repository interface.
 * It shields the Domain layer from the technical complexities of Spring Data and Hibernate,
 * ensuring that business rules remain agnostic of the database technology.</p>
 *
 * <p><b>DESIGN DECISIONS:</b></p>
 * <ul>
 * <li><b>Dependency Inversion:</b> Implements a Domain interface while residing in the
 * Infrastructure layer, following the Dependency Inversion Principle (DIP).</li>
 * <li><b>Transaction Support:</b> Leverages Spring Data's implicit transaction management
 * to ensure that changes to the Account aggregate (and its version) are persisted atomically.</li>
 * <li><b>Optimistic Locking Compatibility:</b> Delegates 'save' operations to JPA,
 * which automatically triggers the '@Version' check to prevent lost updates in concurrent environments.</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository springDataAccountRepository;

    /**
     * Retrieves an Account aggregate by its internal technical ID.
     * * @param id The UUID of the account.
     * @return An Optional containing the Account aggregate if found.
     */
    @Override
    public Optional<Account> findById(UUID id) {
        return springDataAccountRepository.findById(id);
    }

    /**
     * Persists or updates the Account aggregate in the database.
     * <p>When updating, Hibernate will compare the current 'version' field to
     * ensure no other process has modified the account since it was loaded.</p>
     * * @param account The Account aggregate root to be saved.
     */
    @Override
    public void save(Account account) {
        springDataAccountRepository.save(account);
    }

    /**
     * [Implementation Note] If the Domain Port 'AccountRepository' includes more methods
     * (like findByAccountNumber), they must be implemented here by delegating
     * to 'springDataAccountRepository'.
     */
}