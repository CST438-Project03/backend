package com.example.proj3.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserTests {

    @Test
    void testUserCreation() {
        User user = new User();
        assertNotNull(user, "User object should be created");
    }

    @Test
    void testUserAttributes() {
        // Create user with all attributes set
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword123");
        user.setAdmin(false);
        user.setPasswordSetDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        user.setOAuthUser(false);

        // Test getters
        assertEquals(1L, user.getId());
        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword123", user.getPassword());
        assertFalse(user.isAdmin());
        assertFalse(user.isOAuthUser());
        assertNotNull(user.getPasswordSetDate());
    }

    @Test
    void testUserBuilder() {
        // Test user builder pattern if it exists
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setAdmin(false);

        assertEquals("testUser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertFalse(user.isAdmin());
    }

    @Test
    void testUserId() {
        User user = new User();
        user.setId(123L);
        assertEquals(123L, user.getId());
    }

    @Test
    void testUserIsAdmin() {
        User user = new User();
        user.setAdmin(true);
        assertTrue(user.isAdmin());

        user.setAdmin(false);
        assertFalse(user.isAdmin());
    }

    @Test
    void testUserIsOAuthUser() {
        User user = new User();
        user.setOAuthUser(true);
        assertTrue(user.isOAuthUser());

        user.setOAuthUser(false);
        assertFalse(user.isOAuthUser());
    }

    @Test
    void testPasswordSetDate() {
        User user = new User();
        String date = "2025-01-01";
        user.setPasswordSetDate(date);
        assertEquals(date, user.getPasswordSetDate());
    }
}