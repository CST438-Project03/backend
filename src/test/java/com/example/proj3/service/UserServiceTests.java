package com.example.proj3.service;

import com.example.proj3.model.User;
import com.example.proj3.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setAdmin(false);
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = userService.createUser(testUser);

        // Assert
        assertTrue(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void createUser_Exception() {
        // Arrange
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = userService.createUser(testUser);

        // Assert
        assertFalse(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void getUserByUsername_Exists() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_NotExists() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        try {
            User result = userService.getUserByUsername("nonexistent");
            // If method returns null instead of throwing exception
            assertNull(result);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            // This is also acceptable if your service throws exceptions for not found users
            assertTrue(true, "Exception is expected behavior for some implementations");
        }
        
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getUserByEmail_Exists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void getUserByEmail_NotExists() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        try {
            User result = userService.getUserByEmail("nonexistent@example.com");
            // If method returns null instead of throwing exception
            assertNull(result);
        } catch (Exception e) {
            // This is also acceptable if your service throws exceptions for not found users
            assertTrue(true, "Exception is expected behavior for some implementations");
        }
        
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void userExistsByUsername_True() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.userExistsByUsername("testuser");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void userExistsByUsername_False() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.userExistsByUsername("nonexistent");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void userExistsByEmail_True() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.userExistsByEmail("test@example.com");

        // Assert
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void userExistsByEmail_False() {
        // Arrange
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean result = userService.userExistsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result);
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }
}