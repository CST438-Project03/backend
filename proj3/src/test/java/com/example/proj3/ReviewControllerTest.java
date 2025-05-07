package com.example.proj3.controller;

import com.example.proj3.model.Review;
import com.example.proj3.model.User;
import com.example.proj3.model.VideoGame;
import com.example.proj3.service.ReviewService;
import com.example.proj3.service.UserService;
import com.example.proj3.service.VideoGameService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private UserService userService;

    @MockBean
    private VideoGameService videoGameService;

    private User mockUser;
    private VideoGame mockGame;
    private Review mockReview;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockGame = new VideoGame();
        mockGame.setId(100L);
        mockGame.setTitle("Test Game");

        mockReview = new Review(mockUser, mockGame, 9, "Awesome!");
        mockReview.setId(200L);
    }

    // -------------------- Happy Path --------------------

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReview_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(videoGameService.findById(100L)).thenReturn(Optional.of(mockGame));
        when(reviewService.createReview(any(), any(), eq(9), eq("Awesome!"))).thenReturn(mockReview);

        mockMvc.perform(post("/api/reviews/create/game/100")
                        .param("rating", "9")
                        .param("comment", "Awesome!"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Review created successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetReviewsForGame_Success() throws Exception {
        when(reviewService.getReviewsForGame(100L)).thenReturn(List.of(mockReview));

        mockMvc.perform(get("/api/reviews/game/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].comment").value("Awesome!"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetUserReviews_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(reviewService.getUserReviews(mockUser)).thenReturn(List.of(mockReview));

        mockMvc.perform(get("/api/reviews/all/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    // -------------------- Error Handling --------------------

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReview_InvalidRating() throws Exception {
        mockMvc.perform(post("/api/reviews/create/game/100")
                        .param("rating", "0")
                        .param("comment", "Terrible"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be between 1 and 10"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateReview_BlankComment() throws Exception {
        mockMvc.perform(post("/api/reviews/create/game/100")
                        .param("rating", "5")
                        .param("comment", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Comment cannot be empty"));
    }

    @Test
    void testGetUserReviews_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/reviews/all/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteReview_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        Mockito.doNothing().when(reviewService).deleteReview(200L, mockUser);

        mockMvc.perform(delete("/api/reviews/delete/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testEditReview_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(reviewService.editReview(eq(200L), eq(mockUser), eq(10), eq("Updated!"))).thenReturn(mockReview);

        mockMvc.perform(put("/api/reviews/edit/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(
                                new ReviewPayload(10, "Updated!")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review updated successfully"));
    }

    static class ReviewPayload {
        public int rating;
        public String comment;

        public ReviewPayload(int rating, String comment) {
            this.rating = rating;
            this.comment = comment;
        }
    }
}
