package com.example.proj3.controller;

import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.service.UserService;
import com.example.proj3.model.VideoGame;
import com.example.proj3.service.ReviewService;
import com.example.proj3.service.VideoGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final VideoGameService videoGameService;
    private final  UserService userService;


    @Autowired
    public ReviewController(ReviewService reviewService, VideoGameService videoGameService, UserService userService) {
        this.reviewService = reviewService;
        this.videoGameService = videoGameService;
        this.userService = userService;
    }

    //creates review
    @PostMapping("create/game/{gameId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long gameId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        if (userDetails == null) {
            response.put("message", "You must be logged in to submit a review");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Attempt to retrieve the full User entity
        User user = userService.getUserByUsername(userDetails.getUsername());
        System.out.println("Looking up user: " + userDetails.getUsername());
        System.out.println("Found user: " + (user != null ? user.getUsername() : "null"));

        if (user == null) {
            response.put("message", "Authenticated user not found in the database");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        System.out.println("Incoming rating: " + rating);
        System.out.println("Incoming comment: " + comment);

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
            VideoGame videoGame;

            if (gameOpt.isEmpty()) {
                // Game not in DB — fetch from RAWG and save
                try {
                    videoGame = videoGameService.fetchAndSaveFromRawg(gameId);
                    System.out.println("Fetched game from RAWG and saved: " + videoGame.getTitle());
                } catch (Exception e) {
                    response.put("message", "Failed to fetch and save game: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            } else {
                videoGame = gameOpt.get();
            }

            Review review = reviewService.createReview(user, videoGame, rating, comment);

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
    public ResponseEntity<?> getUserReviews(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("message", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Fetch full User entity
            User user = userService.getUserByUsername(userDetails.getUsername());

            if (user == null) {
                response.put("message", "Authenticated user not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

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
    //edit reveiew
    @PutMapping("edit/{reviewId}")
    public ResponseEntity<?> editReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> payload) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("UserDetails: " + (userDetails != null ? userDetails.getUsername() : "null"));

        try {
            if (userDetails == null) {
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extract rating and comment from JSON payload
            Integer rating = (Integer) payload.get("rating");
            String comment = (String) payload.get("comment");

            if (rating == null || comment == null || comment.trim().isEmpty()) {
                response.put("message", "Invalid rating or comment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.getUserByUsername(userDetails.getUsername());
            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Review updatedReview = reviewService.editReview(reviewId, user, rating, comment);

            response.put("message", "Review updated successfully");
            response.put("review", updatedReview);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to edit review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //delete review not currently working
    @DeleteMapping("delete/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        System.out.println("UserDetails: " + (userDetails != null ? userDetails.getUsername() : "null"));

        try {
            if (userDetails == null) {
                response.put("message", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userService.getUserByUsername(userDetails.getUsername());
            if (user == null) {
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            reviewService.deleteReview(reviewId, user);

            response.put("message", "Review deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to delete review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}