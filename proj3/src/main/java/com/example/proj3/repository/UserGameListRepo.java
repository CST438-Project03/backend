package com.example.proj3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.proj3.model.User;
import com.example.proj3.model.UserGameList;

@Repository
public interface UserGameListRepo extends JpaRepository<UserGameList, Long> {
    List<UserGameList> findByUser(User user); // get all lists for a user

    Optional<UserGameList> findByIdAndUser(Long id, User user); // secure fetching
}
