package com.example.proj3.controller;

import com.example.proj3.model.Review;
import com.example.proj3.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/reviews")
public class PublicReviewController {
    
    private final ReviewService reviewService;
    
    @Autowired
    public PublicReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }
    
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> reviews = reviewService.getRecentReviews(limit);
            
            response.put("message", "Recent reviews retrieved successfully");
            response.put("reviews", reviews);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to get recent reviews: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}