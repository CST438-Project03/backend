
package com.example.proj3.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.proj3.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByIsAdminTrue();

}