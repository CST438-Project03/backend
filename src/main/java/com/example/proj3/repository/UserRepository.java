package com.example.proj3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.example.proj3.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByIsAdminTrue();

    List<User> findByUsernameContainingIgnoreCase(String query);
}