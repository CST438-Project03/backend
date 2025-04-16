package com.example.proj3.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.ReviewRepo;
import com.example.proj3.repository.VideoGameRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final VideoGameRepo gameRepo;

    public ReviewService(ReviewRepo reviewRepo, VideoGameRepo gameRepo) {
        this.reviewRepo = reviewRepo;
        this.gameRepo = gameRepo;
    }
    //Create a new review if it doesn't already exist for the user and game
    public Review createReview(User user, VideoGame videoGame, int rating, String comment) {
        Optional<Review> existing = reviewRepo.findByUserAndVideoGame(user, videoGame);
        if (existing.isPresent()) {
            throw new RuntimeException("Review already exists");
        }

        Review review = new Review(user, videoGame, rating, comment);
        return reviewRepo.save(review);
    }
    // Get all reviews for a specific game
    public List<Review> getReviewsForGame(Long gameId) {
        Optional<VideoGame> gameOpt = gameRepo.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found");
        }
        return reviewRepo.findByVideoGame(gameOpt.get());
    }

    // Get all reviews created by a user
    public List<Review> getUserReviews(User user) {

        return reviewRepo.findByUser(user);
    }

    public void deleteReview(Long reviewId, User user) {
        Optional<Review> reviewOpt = reviewRepo.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();
        if (!review.getUser().equals(user)) {
            throw new RuntimeException("You can only delete your own review");
        }

        reviewRepo.delete(review);
    }

    public Review editReview(Long reviewId, User user, int newRating, String newComment) {
        Optional<Review> reviewOpt = reviewRepo.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            throw new RuntimeException("Review not found");
        }

        Review review = reviewOpt.get();
        if (!review.getUser().equals(user)) {
            throw new RuntimeException("You can only edit your own review");
        }

        review.setRating(newRating);
        review.setComment(newComment);
        return reviewRepo.save(review);
    }
}
