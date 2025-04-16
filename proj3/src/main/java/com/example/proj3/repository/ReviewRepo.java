package com.example.proj3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.proj3.model.Review;
import com.example.proj3.model.VideoGame;
import com.example.proj3.model.User;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {
    List<Review> findByVideoGame(VideoGame videoGame); // Find all reviews for a specific game
    List<Review> findByUser(User user); // Find all reviews by a specific user
    Optional<Review> findByUserAndVideoGame(User user, VideoGame videoGame); // to prevent duplicate reviews
}
