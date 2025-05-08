package com.example.proj3.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthenticationResponseTests {

    @Test
    void constructorShouldSetAccessToken() {
        // Arrange & Act
        String testToken = "test.jwt.token";
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(testToken);
        
        // Assert
        assertEquals(testToken, response.getAccessToken());
    }
    
    @Test
    void setAccessTokenShouldUpdateToken() {
        // Arrange
        String initialToken = "initial.token";
        String updatedToken = "updated.token";
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(initialToken);
        
        // Act
        response.setAccessToken(updatedToken);
        
        // Assert
        assertEquals(updatedToken, response.getAccessToken());
    }
    
    @Test
    void getAccessTokenShouldReturnCurrentToken() {
        // Arrange
        String testToken = "current.token";
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(testToken);
        
        // Act & Assert
        String retrievedToken = response.getAccessToken();
        assertEquals(testToken, retrievedToken);
    }
    
    @Test
    void shouldHandleNullToken() {
        // Arrange & Act
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(null);
        
        // Assert
        assertNull(response.getAccessToken());
        
        // Act again - set null on an existing token
        response.setAccessToken("token");
        response.setAccessToken(null);
        
        // Assert again
        assertNull(response.getAccessToken());
    }
    
    @Test
    void shouldHandleEmptyToken() {
        // Arrange & Act
        String emptyToken = "";
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(emptyToken);
        
        // Assert
        assertEquals(emptyToken, response.getAccessToken());
    }
}