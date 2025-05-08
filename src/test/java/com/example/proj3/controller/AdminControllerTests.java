package com.example.proj3.controller;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.repository.UserRepository;
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

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AdminControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

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

    @Mock
    private Principal principal;

    @InjectMocks
    private AdminController adminController;

    private User testUser;
    private User adminUser;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Set up regular test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setAdmin(false);

        // Set up admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setAdmin(true);

        // Set up user list
        userList = Arrays.asList(testUser, adminUser);

        // Mock principal to return admin username
        when(principal.getName()).thenReturn("adminuser");
        when(userService.getUserByUsername("adminuser")).thenReturn(adminUser);

        // Mock authentication
        when(userDetails.getUsername()).thenReturn("adminuser");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(userList);

        // Act
        ResponseEntity<List<User>> response = adminController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(testUser.getId(), response.getBody().get(0).getId());
        assertEquals(adminUser.getId(), response.getBody().get(1).getId());
        
        // Verify service was called
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_Exception() {
        // Arrange
        when(userService.getAllUsers()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<User>> response = adminController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        // Verify service was called
        verify(userService).getAllUsers();
    }

    @Test
    void createUser_Success() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        userData.put("email", "newuser@example.com");
        userData.put("password", "password123");
        userData.put("admin", true);

        when(userService.userExistsByUsername("newuser")).thenReturn(false);
        when(userService.userExistsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("encodedPassword");
        newUser.setAdmin(true);
        
        when(userService.saveUser(any(User.class))).thenReturn(newUser);

        // Act
        ResponseEntity<User> response = adminController.createUser(userData);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("newuser@example.com", response.getBody().getEmail());
        assertTrue(response.getBody().isAdmin());
        
        // Verify service was called
        verify(userService).userExistsByUsername("newuser");
        verify(userService).userExistsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void createUser_UserExists() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "existinguser");
        userData.put("email", "existing@example.com");
        userData.put("password", "password123");
        userData.put("admin", true);

        when(userService.userExistsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<User> response = adminController.createUser(userData);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        
        // Verify service was called
        verify(userService).userExistsByUsername("existinguser");
        verify(userService, never()).saveUser(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        ResponseEntity<Map<String, String>> response = adminController.deleteUser(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody().get("message"));
        
        // Verify repository was called
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, String>> response = adminController.deleteUser(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
        
        // Verify repository was called but delete was not
        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void grantAdminPrivileges_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.grantAdminPrivileges(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Admin privileges granted successfully", response.getBody().get("message"));
        assertTrue(testUser.isAdmin()); // User should now be admin
        
        // Verify repository was called
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void grantAdminPrivileges_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.grantAdminPrivileges(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
        
        // Verify repository was called but save was not
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void revokeAdminPrivileges_Success() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(adminUser)).thenReturn(adminUser);

        // Act
        ResponseEntity<Map<String, Object>> response = adminController.revokeAdminPrivileges(2L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Admin privileges revoked successfully", response.getBody().get("message"));
        assertFalse(adminUser.isAdmin()); // User should no longer be admin
        
        // Verify repository was called
        verify(userRepository).findById(2L);
        verify(userRepository).save(adminUser);
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<User> response = adminController.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());
        assertEquals(testUser.getUsername(), response.getBody().getUsername());
        
        // Verify repository was called
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<User> response = adminController.getUserById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        
        // Verify repository was called
        verify(userRepository).findById(999L);
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "updateduser");
        userData.put("email", "updated@example.com");
        userData.put("admin", true);
        userData.put("password", "newpassword");
        
        when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = adminController.updateUser(1L, userData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("newEncodedPassword", testUser.getPassword());
        assertTrue(testUser.isAdmin());
        
        // Verify repository was called
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_NoPassword() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "updateduser");
        userData.put("email", "updated@example.com");
        userData.put("admin", true);
        // No password in update data
        
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        ResponseEntity<User> response = adminController.updateUser(1L, userData);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updateduser", testUser.getUsername());
        assertEquals("updated@example.com", testUser.getEmail());
        assertEquals("encodedPassword", testUser.getPassword()); // Password should remain unchanged
        assertTrue(testUser.isAdmin());
        
        // Verify repository was called but password encoder was not
        verify(userRepository).findById(1L);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void searchUsers_WithQuery() {
        // Arrange
        when(userService.searchUsers("test")).thenReturn(Collections.singletonList(testUser));

        // Act
        ResponseEntity<List<User>> response = adminController.searchUsers("test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testUser.getId(), response.getBody().get(0).getId());
        
        // Verify service was called
        verify(userService).searchUsers("test");
    }

    @Test
    void searchUsers_EmptyQuery() {
        // Arrange
        when(userService.getAllUsers()).thenReturn(userList);

        // Act
        ResponseEntity<List<User>> response = adminController.searchUsers("");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        
        // Verify getAllUsers was called instead of searchUsers
        verify(userService).getAllUsers();
        verify(userService, never()).searchUsers(anyString());
    }

    @Test
    void createUserAccount_AsAdmin_Success() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        userData.put("email", "newuser@example.com");
        userData.put("password", "password123");
        userData.put("admin", true);

        when(userService.userExistsByUsername("newuser")).thenReturn(false);
        when(userService.userExistsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(true);
        
        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("encodedPassword");
        newUser.setAdmin(true);
        
        when(userService.getUserByUsername("newuser")).thenReturn(newUser);

        // Act
        ResponseEntity<?> response = adminController.createUserAccount(userData, principal);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("newuser", responseBody.get("username"));
        assertEquals("newuser@example.com", responseBody.get("email"));
        assertTrue((Boolean) responseBody.get("isAdmin"));
        assertEquals("test-jwt-token", responseBody.get("jwtToken"));
        
        // Verify service was called
        verify(userService).userExistsByUsername("newuser");
        verify(userService).userExistsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userService).createUser(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    void createUserAccount_AsNonAdmin_NoAdminPrivileges() {
        // Arrange
        // Set up non-admin principal
        when(principal.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        userData.put("email", "newuser@example.com");
        userData.put("password", "password123");
        userData.put("admin", true); // Non-admin tries to create admin user

        when(userService.userExistsByUsername("newuser")).thenReturn(false);
        when(userService.userExistsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(true);
        
        User newUser = new User();
        newUser.setId(3L);
        newUser.setUsername("newuser");
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("encodedPassword");
        newUser.setAdmin(false); // Should be created as non-admin regardless of request
        
        when(userService.getUserByUsername("newuser")).thenReturn(newUser);

        // Act
        ResponseEntity<?> response = adminController.createUserAccount(userData, principal);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("newuser", responseBody.get("username"));
        assertEquals("newuser@example.com", responseBody.get("email"));
        assertFalse((Boolean) responseBody.get("isAdmin")); // User should not be admin
        
        // Verify user was created without admin privileges
        verify(userService).createUser(argThat(user -> !user.isAdmin()));
    }

    @Test
    void createUserAccount_UserExists() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "existinguser");
        userData.put("email", "new@example.com");
        userData.put("password", "password123");

        when(userService.userExistsByUsername("existinguser")).thenReturn(true);

        // Act
        ResponseEntity<?> response = adminController.createUserAccount(userData, principal);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username already taken", responseBody.get("message"));
        
        // Verify service was called but user was not created
        verify(userService).userExistsByUsername("existinguser");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUserAccount_MissingFields() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newuser");
        // Missing email and password

        // Act
        ResponseEntity<?> response = adminController.createUserAccount(userData, principal);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username, email, and password are required", responseBody.get("message"));
        
        // Verify service was not called
        verify(userService, never()).createUser(any(User.class));
    }
}