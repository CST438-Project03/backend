package com.example.proj3.controller;

import com.example.proj3.model.User;
import com.example.proj3.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private Principal principal;

    @InjectMocks
    private UserController userController;

    @TempDir
    static Path tempDir;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Set up regular test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setAdmin(false);
        testUser.setProfilePicture("/uploads/test-picture.jpg");

        // Set up admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setAdmin(true);

        // Mock principal to return test username
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(userService.getUserByUsername("adminuser")).thenReturn(adminUser);

        // Set upload directory to temp directory for testing
        ReflectionTestUtils.setField(userController, "uploadDir", tempDir.toString());
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.createUser(testUser);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User created successfully", response.getBody());
        
        // Verify service was called
        verify(userService).createUser(testUser);
    }

    @Test
    void createUser_Failure() {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.createUser(testUser);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Failed to create user", response.getBody());
        
        // Verify service was called
        verify(userService).createUser(testUser);
    }

    @Test
    void getUserInfo_Success() {
        // Act
        ResponseEntity<?> response = userController.getUserInfo(principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        
        // Verify service was called
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void getUserInfo_Unauthorized() {
        // Act
        ResponseEntity<?> response = userController.getUserInfo(null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User not authenticated", response.getBody());
        
        // Verify service was not called
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void getUserInfo_UserNotFound() {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.getUserInfo(principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        
        // Verify service was called
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void updateUsername_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("oldUsername", "testuser");
        request.put("newUsername", "newtestuser");

        when(userService.getUserByUsername("newtestuser")).thenReturn(null);
        when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(1);
            user.setUsername("newtestuser");
            return user;
        });

        // Act
        ResponseEntity<?> response = userController.updateUsername(request, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("newtestuser", responseBody.get("username"));
        assertEquals("Username updated successfully", responseBody.get("message"));
        
        // Verify service was called
        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    void updateUsername_UsernameTaken() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("oldUsername", "testuser");
        request.put("newUsername", "takenuser");

        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("takenuser");
        
        when(userService.getUserByUsername("takenuser")).thenReturn(otherUser);

        // Act
        ResponseEntity<?> response = userController.updateUsername(request, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already taken", response.getBody());
        
        // Verify service was called but update was not
        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }

    @Test
    void updateEmail_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("oldEmail", "test@example.com");
        request.put("newEmail", "new@example.com");

        when(userService.getUserByEmail("new@example.com")).thenReturn(null);
        when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(1);
            user.setEmail("new@example.com");
            return user;
        });

        // Act
        ResponseEntity<?> response = userController.updateEmail(request, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("new@example.com", responseBody.get("email"));
        assertEquals("Email updated successfully", responseBody.get("message"));
        
        // Verify service was called
        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    void updateEmail_EmailTaken() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("oldEmail", "test@example.com");
        request.put("newEmail", "taken@example.com");

        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setEmail("taken@example.com");
        
        when(userService.getUserByEmail("taken@example.com")).thenReturn(otherUser);

        // Act
        ResponseEntity<?> response = userController.updateEmail(request, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already taken", response.getBody());
        
        // Verify service was called but update was not
        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }

    @Test
    void updateProfilePicture_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "profilePicture",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        when(userService.updateUser(eq(1L), any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(1);
            // The exact URL will vary due to UUID, so we just need to check it's updated
            assertNotNull(user.getProfilePicture());
            assertTrue(user.getProfilePicture().startsWith("/uploads/"));
            return user;
        });

        // Act
        ResponseEntity<?> response = userController.updateProfilePicture(file, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(((String)responseBody.get("profilePicture")).startsWith("/uploads/"));
        assertEquals("Profile picture updated successfully", responseBody.get("message"));
        
        // Verify service was called
        verify(userService).updateUser(eq(1L), any(User.class));
    }

    @Test
    void updatePassword_Success_OwnAccount() {
        // Arrange
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "password123");
        passwordData.put("newPassword", "newpassword123");

        when(userService.updatePassword(1L, "password123", "newpassword123")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.updatePassword(1L, passwordData, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody());
        
        // Verify service was called
        verify(userService).updatePassword(1L, "password123", "newpassword123");
    }

    @Test
    void updatePassword_Success_AdminUser() {
        // Arrange
        when(principal.getName()).thenReturn("adminuser");
        
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "notneeded");
        passwordData.put("newPassword", "newpassword123");

        when(userService.resetPassword(1L, "newpassword123")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.updatePassword(1L, passwordData, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody());
        
        // Verify service was called with reset instead of update
        verify(userService).resetPassword(1L, "newpassword123");
        verify(userService, never()).updatePassword(anyLong(), anyString(), anyString());
    }

    @Test
    void updatePassword_Forbidden() {
        // Arrange
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "password123");
        passwordData.put("newPassword", "newpassword123");

        // Act
        ResponseEntity<?> response = userController.updatePassword(2L, passwordData, principal);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Not authorized to update this user's password", response.getBody());
        
        // Verify service was not called
        verify(userService, never()).updatePassword(anyLong(), anyString(), anyString());
        verify(userService, never()).resetPassword(anyLong(), anyString());
    }

    @Test
    void checkPasswordStatus_Success() {
        // Arrange
        testUser.setOAuthUser(true);
        testUser.setPasswordSetDate("2023-05-01");
        testUser.setOauthProvider("google");

        // Act
        ResponseEntity<?> response = userController.checkPasswordStatus(principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) responseBody.get("isOAuthUser"));
        assertTrue((Boolean) responseBody.get("hasSetPassword"));
        assertEquals("google", responseBody.get("oauthProvider"));
        
        // Verify service was called
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void createPassword_Success_OAuthUser() {
        // Arrange
        testUser.setOAuthUser(true);
        testUser.setPasswordSetDate(null);
        
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("newPassword", "newpassword123");

        when(userService.setPassword(1L, "newpassword123")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.createPassword(passwordData, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password set successfully", response.getBody());
        
        // Verify service was called
        verify(userService).setPassword(1L, "newpassword123");
        verify(userService, never()).checkPassword(anyLong(), anyString());
    }

    @Test
    void createPassword_Success_RegularUser() {
        // Arrange
        testUser.setOAuthUser(false);
        testUser.setPasswordSetDate("2023-05-01");
        
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "password123");
        passwordData.put("newPassword", "newpassword123");

        when(userService.checkPassword(1L, "password123")).thenReturn(true);
        when(userService.setPassword(1L, "newpassword123")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.createPassword(passwordData, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password set successfully", response.getBody());
        
        // Verify services were called
        verify(userService).checkPassword(1L, "password123");
        verify(userService).setPassword(1L, "newpassword123");
    }

    @Test
    void requestPasswordReset_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");

        when(userService.requestPasswordReset("test@example.com")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.requestPasswordReset(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("If your email is registered, you will receive password reset instructions", response.getBody());
        
        // Verify service was called
        verify(userService).requestPasswordReset("test@example.com");
    }

    @Test
    void confirmPasswordReset_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("token", "valid-reset-token");
        request.put("newPassword", "newpassword123");

        when(userService.confirmPasswordReset("valid-reset-token", "newpassword123")).thenReturn(true);

        // Act
        ResponseEntity<?> response = userController.confirmPasswordReset(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been reset successfully", response.getBody());
        
        // Verify service was called
        verify(userService).confirmPasswordReset("valid-reset-token", "newpassword123");
    }

    @Test
    void confirmPasswordReset_InvalidToken() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("token", "invalid-token");
        request.put("newPassword", "newpassword123");

        when(userService.confirmPasswordReset("invalid-token", "newpassword123")).thenReturn(false);

        // Act
        ResponseEntity<?> response = userController.confirmPasswordReset(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired token", response.getBody());
        
        // Verify service was called
        verify(userService).confirmPasswordReset("invalid-token", "newpassword123");
    }

    @Test
    void searchUsers_Success() {
        // Arrange
        List<User> usersList = Arrays.asList(testUser);
        when(userService.searchUsersByUsername("test")).thenReturn(usersList);

        // Act
        ResponseEntity<?> response = userController.searchUsers("test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usersList, response.getBody());
        
        // Verify service was called
        verify(userService).searchUsersByUsername("test");
    }

    @Test
    void deleteUser_Success_OwnAccount() {
        // Arrange
        when(userService.deleteUser(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L, principal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify service was called
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_Success_AsAdmin() {
        // Arrange
        when(principal.getName()).thenReturn("adminuser");
        when(userService.deleteUser(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L, principal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify service was called
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_Forbidden() {
        // Arrange
        when(userService.deleteUser(2L)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(2L, principal);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        // Verify service was not called
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    void getAllUsernames_Success() {
        // Arrange
        List<String> usernames = Arrays.asList("user1", "user2", "user3");
        when(userService.getAllUsernames()).thenReturn(usernames);

        // Act
        ResponseEntity<?> response = userController.getAllUsernames();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(usernames, response.getBody());
        
        // Verify service was called
        verify(userService).getAllUsernames();
    }

    @Test
    void getUserById_Success() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("anotheruser");
        
        when(userService.getUserById(3L)).thenReturn(anotherUser);

        // Act
        ResponseEntity<?> response = userController.getUserById(3L, principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(anotherUser, response.getBody());
        
        // Verify service was called
        verify(userService).getUserById(3L);
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = userController.getUserById(999L, principal);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
        
        // Verify service was called
        verify(userService).getUserById(999L);
    }

    @Test
    void deleteCurrentUser_Success() {
        // Arrange
        when(userService.deleteUser(1L)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = userController.deleteCurrentUser(principal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify service was called
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteCurrentUserPost_Success() {
        // Arrange
        when(userService.deleteUser(1L)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, String>> response = userController.deleteCurrentUserPost(principal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account deleted successfully", response.getBody().get("message"));
        
        // Verify service was called
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteCurrentUserPost_Failure() {
        // Arrange
        when(userService.deleteUser(1L)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, String>> response = userController.deleteCurrentUserPost(principal);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to delete account", response.getBody().get("message"));
        
        // Verify service was called
        verify(userService).deleteUser(1L);
    }
}