package com.example.proj3.controller;

import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.model.VideoGame;
import com.example.proj3.service.ReviewService;
import com.example.proj3.service.VideoGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final VideoGameService videoGameService;

    @Autowired
    public ReviewController(ReviewService reviewService, VideoGameService videoGameService) {
        this.reviewService = reviewService;
        this.videoGameService = videoGameService;
    }

    //creates review
    @PostMapping("create/game/{gameId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long gameId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @AuthenticationPrincipal User user) {

        Map<String, Object> response = new HashMap<>();

        // Validate rating
        if (rating == null || rating < 1 || rating > 10) {
            response.put("message", "Rating must be between 1 and 10");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate comment
        if (comment == null || comment.trim().isEmpty()) {
            response.put("message", "Comment cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Optional<VideoGame> gameOpt = videoGameService.findById(gameId);
            if (gameOpt.isEmpty()) {
                response.put("message", "Game not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Review review = reviewService.createReview(user, gameOpt.get(), rating, comment);

            response.put("message", "Review created successfully");
            response.put("review", review);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Review already exists")) {
                response.put("message", "You have already reviewed this game");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                response.put("message", "Failed to create review: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //gets reviews for specfic game
    @GetMapping("/game/{gameId}")
    public ResponseEntity<?> getReviewsForGame(@PathVariable Long gameId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Review> reviews = reviewService.getReviewsForGame(gameId);

            response.put("message", "Reviews retrieved successfully");
            response.put("reviews", reviews);
            response.put("count", reviews.size());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Game not found")) {
                response.put("message", "Game not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "Failed to get reviews: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //gets all reviews from a user
    @GetMapping("all/user")
    public ResponseEntity<?> getUserReviews(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Review> reviews = reviewService.getUserReviews(user);

            response.put("message", "User reviews retrieved successfully");
            response.put("reviews", reviews);
            response.put("count", reviews.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to get user reviews: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //edit reveiew
    @PutMapping("edit/{reviewId}")
    public ResponseEntity<?> editReview(
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @AuthenticationPrincipal User user) {

        Map<String, Object> response = new HashMap<>();

        // Validate rating
        if (rating == null || rating < 1 || rating > 10) {
            response.put("message", "Rating must be between 1 and 10");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate comment
        if (comment == null || comment.trim().isEmpty()) {
            response.put("message", "Comment cannot be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Review updatedReview = reviewService.editReview(reviewId, user, rating, comment);

            response.put("message", "Review updated successfully");
            response.put("review", updatedReview);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Review not found")) {
                response.put("message", "Review not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().contains("You can only edit")) {
                response.put("message", "You can only edit your own reviews");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            } else {
                response.put("message", "Failed to update review: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //delete review not currently working
    @DeleteMapping("delete/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        Map<String, String> response = new HashMap<>();

        try {
            reviewService.deleteReview(reviewId, user);

            response.put("message", "Review deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Review not found")) {
                response.put("message", "Review not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().contains("You can only delete")) {
                response.put("message", "You can only delete your own reviews");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            } else {
                response.put("message", "Failed to delete review: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}