package io.github.erikstrnad.userapi.repository;

import io.github.erikstrnad.userapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    // Finds a user by their username
    Optional<User> findByUsername(String username);
}
