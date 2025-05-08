package com.example.proj3;

import com.example.proj3.controller.ReviewController;
import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.model.VideoGame;
import com.example.proj3.service.ReviewService;
import com.example.proj3.service.UserService;
import com.example.proj3.service.VideoGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private VideoGameService videoGameService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ReviewController reviewController;

    private User testUser;
    private VideoGame testGame;
    private Review testReview;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGame = new VideoGame();
        testGame.setId(100L);
        testGame.setTitle("Test Game");

        testReview = new Review(testUser, testGame, 8, "Great game!");
        testReview.setId(1L);

        // Set up the common mock behavior - this is okay for strict Mockito if all tests use it
        lenient().when(userDetails.getUsername()).thenReturn("testUser");
    }

    @Test
    void createReview_Success() {
        // Arrange - only mock what's needed for this test
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        when(videoGameService.findById(100L)).thenReturn(Optional.of(testGame));
        when(reviewService.createReview(eq(testUser), eq(testGame), eq(8), eq("Great comment"))).thenReturn(testReview);

        // Act
        ResponseEntity<?> response = reviewController.createReview(100L, 8, "Great comment", userDetails);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Review created successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("review"));

        // Only verify what's relevant for this test
        verify(userService, times(1)).getUserByUsername("testUser");
        verify(videoGameService, times(1)).findById(100L);
        verify(reviewService, times(1)).createReview(testUser, testGame, 8, "Great comment");
    }

    @Test
    void createReview_GameNotInDB_FetchFromRawg() {
        // Arrange
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        when(videoGameService.findById(100L)).thenReturn(Optional.empty());
        when(videoGameService.fetchAndSaveFromRawg(100L)).thenReturn(testGame);
        when(reviewService.createReview(eq(testUser), eq(testGame), eq(8), eq("Great comment"))).thenReturn(testReview);

        // Act
        ResponseEntity<?> response = reviewController.createReview(100L, 8, "Great comment", userDetails);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Review created successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("review"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(videoGameService, times(1)).findById(100L);
        verify(videoGameService, times(1)).fetchAndSaveFromRawg(100L);
        verify(reviewService, times(1)).createReview(testUser, testGame, 8, "Great comment");
    }

    @Test
    void createReview_UserNotAuthenticated() {
        // Act
        ResponseEntity<?> response = reviewController.createReview(100L, 8, "Great comment", null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("You must be logged in to submit a review", responseBody.get("message"));

        verify(userService, never()).getUserByUsername(anyString());
        verify(videoGameService, never()).findById(anyLong());
        verify(reviewService, never()).createReview(any(), any(), anyInt(), anyString());
    }

    @Test
    void createReview_UserNotFound() {
        // Arrange
        when(userService.getUserByUsername("testUser")).thenReturn(null);

        // Act
        ResponseEntity<?> response = reviewController.createReview(100L, 8, "Great comment", userDetails);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Authenticated user not found in the database", responseBody.get("message"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(videoGameService, never()).findById(anyLong());
        verify(reviewService, never()).createReview(any(), any(), anyInt(), anyString());
    }

    @Test
    void createReview_InvalidRating() {
        // Arrange
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);

        // Act - Test with rating below 1
        ResponseEntity<?> response = reviewController.createReview(100L, 0, "Great comment", userDetails);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Rating must be between 1 and 10", responseBody.get("message"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(videoGameService, never()).findById(anyLong());
        verify(reviewService, never()).createReview(any(), any(), anyInt(), anyString());
    }

    @Test
    void createReview_EmptyComment() {
        // Arrange
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = reviewController.createReview(100L, 8, "", userDetails);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Comment cannot be empty", responseBody.get("message"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(videoGameService, never()).findById(anyLong());
        verify(reviewService, never()).createReview(any(), any(), anyInt(), anyString());
    }

    @Test
    void getReviewsForGame_Success() {
        // Arrange
        List<Review> reviews = new ArrayList<>();
        reviews.add(testReview);

        when(reviewService.getReviewsForGame(100L)).thenReturn(reviews);

        // Act
        ResponseEntity<?> response = reviewController.getReviewsForGame(100L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Reviews retrieved successfully", responseBody.get("message"));
        assertEquals(1, responseBody.get("count"));
        assertNotNull(responseBody.get("reviews"));

        verify(reviewService, times(1)).getReviewsForGame(100L);
    }

    @Test
    void getReviewsForGame_GameNotFound() {
        // Arrange
        when(reviewService.getReviewsForGame(999L)).thenThrow(new RuntimeException("Game not found"));

        // Act
        ResponseEntity<?> response = reviewController.getReviewsForGame(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Game not found", responseBody.get("message"));

        verify(reviewService, times(1)).getReviewsForGame(999L);
    }

    @Test
    void getUserReviews_Success() {
        // Arrange
        List<Review> reviews = new ArrayList<>();
        reviews.add(testReview);

        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        when(reviewService.getUserReviews(testUser)).thenReturn(reviews);

        // Act
        ResponseEntity<?> response = reviewController.getUserReviews(userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("User reviews retrieved successfully", responseBody.get("message"));
        assertEquals(1, responseBody.get("count"));
        assertNotNull(responseBody.get("reviews"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(reviewService, times(1)).getUserReviews(testUser);
    }

    @Test
    void getUserReviews_UserNotAuthenticated() {
        // Act
        ResponseEntity<?> response = reviewController.getUserReviews(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Not authenticated", responseBody.get("message"));

        verify(userService, never()).getUserByUsername(anyString());
        verify(reviewService, never()).getUserReviews(any());
    }

    @Test
    void editReview_Success() {
        // Arrange
        Map<String, Object> payload = new HashMap<>();
        payload.put("rating", 9);
        payload.put("comment", "Updated comment");

        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        when(reviewService.editReview(eq(1L), eq(testUser), eq(9), eq("Updated comment"))).thenReturn(testReview);

        // Act
        ResponseEntity<?> response = reviewController.editReview(1L, userDetails, payload);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Review updated successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("review"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(reviewService, times(1)).editReview(1L, testUser, 9, "Updated comment");
    }

    @Test
    void deleteReview_Success() {
        // Arrange
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        doNothing().when(reviewService).deleteReview(1L, testUser);

        // Act
        ResponseEntity<?> response = reviewController.deleteReview(1L, userDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Review deleted successfully", responseBody.get("message"));

        verify(userService, times(1)).getUserByUsername("testUser");
        verify(reviewService, times(1)).deleteReview(1L, testUser);
    }
}