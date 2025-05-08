package com.example.proj3.service;

import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.model.VideoGame;
import com.example.proj3.repository.ReviewRepo;
import com.example.proj3.repository.VideoGameRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepo reviewRepo;

    @Mock
    private VideoGameRepo gameRepo;

    @InjectMocks
    private ReviewService reviewService;

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
    }

    @Test
    void createReview_Success() {
        // Arrange
        when(reviewRepo.findByUserAndVideoGame(testUser, testGame)).thenReturn(Optional.empty());
        when(reviewRepo.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review created = reviewService.createReview(testUser, testGame, 8, "Great game!");

        // Assert
        assertNotNull(created);
        assertEquals(8, created.getRating());
        assertEquals("Great game!", created.getComment());
        verify(reviewRepo).findByUserAndVideoGame(testUser, testGame);
        verify(reviewRepo).save(any(Review.class));
    }

    @Test
    void createReview_AlreadyExists() {
        // Arrange
        when(reviewRepo.findByUserAndVideoGame(testUser, testGame)).thenReturn(Optional.of(testReview));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testUser, testGame, 9, "Updated comment");
        });

        assertEquals("Review already exists", exception.getMessage());
        verify(reviewRepo).findByUserAndVideoGame(testUser, testGame);
        verify(reviewRepo, never()).save(any(Review.class));
    }

    @Test
    void getReviewsForGame_Success() {
        // Arrange
        List<Review> reviews = new ArrayList<>();
        reviews.add(testReview);

        when(gameRepo.findById(100L)).thenReturn(Optional.of(testGame));
        when(reviewRepo.findByVideoGame(testGame)).thenReturn(reviews);

        // Act
        List<Review> result = reviewService.getReviewsForGame(100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(8, result.get(0).getRating());
        verify(gameRepo).findById(100L);
        verify(reviewRepo).findByVideoGame(testGame);
    }

    @Test
    void getReviewsForGame_GameNotFound() {
        // Arrange
        when(gameRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.getReviewsForGame(999L);
        });

        assertEquals("Game not found", exception.getMessage());
        verify(gameRepo).findById(999L);
        verify(reviewRepo, never()).findByVideoGame(any());
    }

    @Test
    void getUserReviews_Success() {
        // Arrange
        List<Review> reviews = new ArrayList<>();
        reviews.add(testReview);

        when(reviewRepo.findByUserId(1L)).thenReturn(reviews);

        // Act
        List<Review> result = reviewService.getUserReviews(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(8, result.get(0).getRating());
        verify(reviewRepo).findByUserId(1L);
    }

    @Test
    void editReview_Success() {
        // Arrange
        when(reviewRepo.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepo.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review updated = reviewService.editReview(1L, testUser, 9, "Updated comment");

        // Assert
        assertNotNull(updated);
        assertEquals(9, updated.getRating());
        assertEquals("Updated comment", updated.getComment());
        verify(reviewRepo).findById(1L);
        verify(reviewRepo).save(testReview);
    }

    @Test
    void editReview_NotFound() {
        // Arrange
        when(reviewRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.editReview(999L, testUser, 9, "Updated comment");
        });

        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepo).findById(999L);
        verify(reviewRepo, never()).save(any());
    }

    @Test
    void editReview_NotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");

        when(reviewRepo.findById(1L)).thenReturn(Optional.of(testReview));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.editReview(1L, otherUser, 9, "Updated comment");
        });

        assertEquals("You can only edit your own review", exception.getMessage());
        verify(reviewRepo).findById(1L);
        verify(reviewRepo, never()).save(any());
    }

    @Test
    void deleteReview_Success() {
        // Arrange
        when(reviewRepo.findById(1L)).thenReturn(Optional.of(testReview));
        doNothing().when(reviewRepo).delete(testReview);

        // Act
        reviewService.deleteReview(1L, testUser);

        // Assert
        verify(reviewRepo).findById(1L);
        verify(reviewRepo).delete(testReview);
    }

    @Test
    void deleteReview_NotFound() {
        // Arrange
        when(reviewRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.deleteReview(999L, testUser);
        });

        assertEquals("Review not found", exception.getMessage());
        verify(reviewRepo).findById(999L);
        verify(reviewRepo, never()).delete(any());
    }

    @Test
    void deleteReview_NotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");

        when(reviewRepo.findById(1L)).thenReturn(Optional.of(testReview));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.deleteReview(1L, otherUser);
        });

        assertEquals("You can only delete your own review", exception.getMessage());
        verify(reviewRepo).findById(1L);
        verify(reviewRepo, never()).delete(any());
    }
}