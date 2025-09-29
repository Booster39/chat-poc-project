// UserRepository.java
package com.example.chatpoc.repository;

import com.example.chatpoc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(User.UserRole role);

    List<User> findByIsOnlineTrue();

    @Query("SELECT u FROM User u WHERE u.role = 'AGENT' AND u.status = 'ACTIVE'")
    List<User> findAvailableAgents();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

