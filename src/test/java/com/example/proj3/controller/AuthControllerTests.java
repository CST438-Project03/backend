package com.example.proj3.controller;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuthControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setAdmin(false);

        // Mock behavior for authentication
        when(userDetails.getUsername()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");
    }

    @Test
    void registerUser_Success() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        userData.put("email", "newuser@example.com");
        userData.put("password", "password123");

        when(userService.userExistsByUsername("newuser")).thenReturn(false);
        when(userService.userExistsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(userData);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("newuser", responseBody.get("username"));
        assertEquals("newuser@example.com", responseBody.get("email"));
        assertEquals("Registration successful! Please login.", responseBody.get("message"));
        
        // Verify interactions
        verify(userService).userExistsByUsername("newuser");
        verify(userService).userExistsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userService).createUser(any(User.class));
    }

    @Test
    void registerUser_UsernameTaken() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "existinguser");
        userData.put("email", "new@example.com");
        userData.put("password", "password123");

        when(userService.userExistsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(userData);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username already taken", responseBody.get("message"));
        
        // Verify interactions
        verify(userService).userExistsByUsername("existinguser");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_EmailTaken() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        userData.put("email", "existing@example.com");
        userData.put("password", "password123");

        when(userService.userExistsByUsername("newuser")).thenReturn(false);
        when(userService.userExistsByEmail("existing@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.registerUser(userData);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Email already in use", responseBody.get("message"));
        
        // Verify interactions
        verify(userService).userExistsByUsername("newuser");
        verify(userService).userExistsByEmail("existing@example.com");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void registerUser_MissingFields() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        // Missing email and password

        // Act
        ResponseEntity<?> response = authController.registerUser(userData);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username, email, and password are required", responseBody.get("message"));
        
        // Verify interactions
        verify(userService, never()).userExistsByUsername(anyString());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        // Act
        ResponseEntity<?> response = authController.login("testuser", "password123");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("test-jwt-token", responseBody.get("jwtToken"));
        assertEquals("testuser", responseBody.get("username"));
        assertEquals(1L, responseBody.get("userId"));
        
        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(any(UserDetails.class));
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void logout_Success() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");

        // Act
        ResponseEntity<Map<String, String>> response = authController.logout(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully logged out!", response.getBody().get("message"));
        
        // Verify token was invalidated
        verify(jwtUtil).invalidateToken("test-token");
    }

    @Test
    void handleOAuthRedirect_Success() {
        // Act
        ResponseEntity<?> response = authController.handleOAuthRedirect("test-oauth-token");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("test-oauth-token", responseBody.get("token"));
        assertEquals("Successfully authenticated via OAuth2", responseBody.get("message"));
    }

    @Test
    void handleOAuthError_ReturnsError() {
        // Act
        ResponseEntity<?> response = authController.handleOAuthError("authentication_failed");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("authentication_failed", responseBody.get("error"));
    }
}