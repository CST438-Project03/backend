package com.example.proj3.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.repository.UserRepository;
import com.example.proj3.service.UserService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AdminController(UserService userService, UserRepository userRepository, 
                         PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, 
                         JwtUtil jwtUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get all users
     * Corresponds to the fetchUsers() function in AdminScreen
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Create a new user
     * Corresponds to the handleCreateUser() function in AdminScreen
     */
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody Map<String, Object> userData) {
        logger.info("Creating new user from admin panel");
        try {
            String username = (String) userData.get("username");
            String email = (String) userData.get("email");
            String password = (String) userData.get("password");
            Boolean isAdmin = (Boolean) userData.get("admin");

            // Check if user already exists
            if (userService.userExistsByUsername(username) || userService.userExistsByEmail(email)) {
                logger.warn("User with username {} or email {} already exists", username, email);
                return ResponseEntity.badRequest().body(null);
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setAdmin(isAdmin != null ? isAdmin : false);

            User createdUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            logger.error("Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Delete a user
     * Corresponds to the handleDeleteUser() function in AdminScreen
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found"));
            }

            userRepository.delete(user);
            logger.info("User deleted successfully: {}", userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Grant admin privileges
     * Corresponds to the handleToggleAdmin() function in AdminScreen
     */
    @PutMapping("/users/{userId}/grant-admin")
    public ResponseEntity<Map<String, Object>> grantAdminPrivileges(@PathVariable Long userId) {
        logger.info("Granting admin privileges to user: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found"));
            }

            user.setAdmin(true);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin privileges granted successfully");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error granting admin privileges: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to grant admin privileges: " + e.getMessage()));
        }
    }

    /**
     * Revoke admin privileges
     * Corresponds to the handleToggleAdmin() function in AdminScreen
     */
    @PutMapping("/users/{userId}/revoke-admin")
    public ResponseEntity<Map<String, Object>> revokeAdminPrivileges(@PathVariable Long userId) {
        logger.info("Revoking admin privileges from user: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User not found"));
            }

            user.setAdmin(false);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin privileges revoked successfully");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error revoking admin privileges: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to revoke admin privileges: " + e.getMessage()));
        }
    }

    /**
     * Get user by ID
     * To support the UserDetailModal in AdminScreen
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        logger.info("Fetching user with ID: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user by ID: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Update user
     * Additional endpoint that might be useful for the admin panel
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userData) {
        logger.info("Updating user with ID: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                logger.warn("User with ID {} not found", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Update user fields if provided
            if (userData.containsKey("username")) {
                user.setUsername((String) userData.get("username"));
            }
            
            if (userData.containsKey("email")) {
                user.setEmail((String) userData.get("email"));
            }
            
            if (userData.containsKey("admin")) {
                user.setAdmin((Boolean) userData.get("admin"));
            }
            
            // Only update password if provided
            if (userData.containsKey("password") && userData.get("password") != null) {
                String newPassword = (String) userData.get("password");
                if (!newPassword.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                }
            }

            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Search users
     * To support the search functionality in AdminScreen
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        logger.info("Searching users with query: {}", query);
        try {
            if (query == null || query.trim().isEmpty()) {
                return getAllUsers();
            }
            
            List<User> users = userService.searchUsers(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error searching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Creates a new user account
     * Regular user accounts will never have admin privileges
     * Only admin users can create accounts with admin privileges
     */
    @PostMapping("/users/create-account")
    public ResponseEntity<?> createUserAccount(@RequestBody Map<String, Object> userData, Principal principal) {
        logger.info("Admin creating new user account");
        try {
            // Check if the current user is an admin
            User currentUser = null;
            boolean isCurrentUserAdmin = false;
            
            if (principal != null) {
                currentUser = userService.getUserByUsername(principal.getName());
                isCurrentUserAdmin = currentUser != null && currentUser.isAdmin();
            }
            
            String username = (String) userData.get("username");
            String email = (String) userData.get("email");
            String password = (String) userData.get("password");
            Boolean requestedAdmin = (Boolean) userData.get("admin");

            // Validate required fields
            if (username == null || email == null || password == null) {
                logger.warn("Missing required fields for user creation");
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Username, email, and password are required"));
            }
            
            // Check if user already exists
            if (userService.userExistsByUsername(username)) {
                logger.warn("Username {} already exists", username);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Username already taken"));
            }
            
            if (userService.userExistsByEmail(email)) {
                logger.warn("Email {} already exists", email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already in use"));
            }

            // Create the user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            
            // Only set admin privileges if the current user is an admin AND requested admin privileges
            boolean grantAdminPrivileges = isCurrentUserAdmin && requestedAdmin != null && requestedAdmin;
            user.setAdmin(grantAdminPrivileges);
            
            // If not an admin request and admin privileges were requested, log this attempt
            if (requestedAdmin != null && requestedAdmin && !isCurrentUserAdmin) {
                logger.warn("Non-admin user attempted to create account with admin privileges");
            }
            
            // Set password date to track when password was set
            user.setPasswordSetDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            
            // Set OAuth fields to false as this is a regular account
            user.setOAuthUser(false);
            
            boolean success = userService.createUser(user);
            
            if (success) {
                User createdUser = userService.getUserByUsername(username);
                
                // Generate JWT token for immediate login
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
                );
                
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtUtil.generateToken(userDetails);
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", createdUser.getId());
                response.put("username", createdUser.getUsername());
                response.put("email", createdUser.getEmail());
                response.put("isAdmin", createdUser.isAdmin());
                response.put("jwtToken", token);
                response.put("message", "User account created successfully");
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.error("Failed to create user account");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("message", "Failed to create user account"));
            }
        } catch (Exception e) {
            logger.error("Error creating user account: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error creating user account: " + e.getMessage()));
        }
    }
}