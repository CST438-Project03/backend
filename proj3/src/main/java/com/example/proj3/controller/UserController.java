package com.example.proj3.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.Paths.get;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.proj3.model.User;
import com.example.proj3.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${file.upload-dir:./uploads/profile-pictures}")
    private String uploadDir;

    //user registration endpoint
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {

        boolean userCreated = userService.createUser(user);
        if (userCreated) {
            return new ResponseEntity<>("User created successfully", HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Failed to create user", HttpStatus.CONFLICT);
        }   
    }


    //gets the current user info
    @GetMapping("/me")
    public ResponseEntity<?>  getUserInfo(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

     /**
     * Update username endpoint
     */
    @PutMapping("/username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        
        String oldUsername = request.get("oldUsername");
        String newUsername = request.get("newUsername");
        
        logger.info("Update username request: old={}, new={}", oldUsername, newUsername);
        
        // Validate request
        if (oldUsername == null || newUsername == null) {
            return new ResponseEntity<>("Missing username values", HttpStatus.BAD_REQUEST);
        }
        
        User currentUser = userService.getUserByUsername(principal.getName());
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        // Check if old username matches current username
        if (!currentUser.getUsername().equals(oldUsername)) {
            return new ResponseEntity<>("Current username does not match", HttpStatus.BAD_REQUEST);
        }
        
        // Check if new username already exists
        User existingUser = userService.getUserByUsername(newUsername);
        if (existingUser != null && !existingUser.getId().equals(currentUser.getId())) {
            return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
        }
        
        // Update username
        currentUser.setUsername(newUsername);
        User updatedUser = userService.updateUser(currentUser.getId(), currentUser);
        
        if (updatedUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("email", updatedUser.getEmail());
            response.put("message", "Username updated successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to update username", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update email endpoint
     */
    @PutMapping("/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> request, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        
        String oldEmail = request.get("oldEmail");
        String newEmail = request.get("newEmail");
        
        logger.info("Update email request: old={}, new={}", oldEmail, newEmail);
        
        // Validate request
        if (oldEmail == null || newEmail == null) {
            return new ResponseEntity<>("Missing email values", HttpStatus.BAD_REQUEST);
        }
        
        User currentUser = userService.getUserByUsername(principal.getName());
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        // Check if old email matches current email
        if (!currentUser.getEmail().equals(oldEmail)) {
            return new ResponseEntity<>("Current email does not match", HttpStatus.BAD_REQUEST);
        }
        
        // Check if new email already exists
        User existingUser = userService.getUserByEmail(newEmail);
        if (existingUser != null && !existingUser.getId().equals(currentUser.getId())) {
            return new ResponseEntity<>("Email already taken", HttpStatus.CONFLICT);
        }
        
        // Update email
        currentUser.setEmail(newEmail);
        User updatedUser = userService.updateUser(currentUser.getId(), currentUser);
        
        if (updatedUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("email", updatedUser.getEmail());
            response.put("message", "Email updated successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to update email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update profile picture endpoint
     */
    @PutMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(@RequestParam("profilePicture") MultipartFile file, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        
        logger.info("Update profile picture request: filename={}, size={}", file.getOriginalFilename(), file.getSize());
        
        User currentUser = userService.getUserByUsername(principal.getName());
        if (currentUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        try {
            // Create the upload directory if it doesn't exist
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Delete old profile picture if exists
            if (currentUser.getProfilePicture() != null && !currentUser.getProfilePicture().isEmpty()) {
                try {
                    String oldFilePath = currentUser.getProfilePicture().replace("/uploads/", uploadDir + "/");
                    Files.deleteIfExists(get(oldFilePath));
                } catch (Exception e) {
                    logger.warn("Failed to delete old profile picture: {}", e.getMessage());
                }
            }
            
            // Generate a unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, filename);
            
            // Save the file
            Files.copy(file.getInputStream(), filePath);
            
            // Update user profile picture
            String fileUrl = "/uploads/" + filename;
            currentUser.setProfilePicture(fileUrl);
            
            User updatedUser = userService.updateUser(currentUser.getId(), currentUser);
            
            if (updatedUser != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", updatedUser.getId());
                response.put("username", updatedUser.getUsername());
                response.put("email", updatedUser.getEmail());
                response.put("profilePicture", fileUrl);
                response.put("message", "Profile picture updated successfully");
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to update profile picture", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            logger.error("Error uploading profile picture: {}", e.getMessage());
            return new ResponseEntity<>("Error uploading profile picture: " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update password endpoint
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData,
            Principal principal) {
        
        // Check if user is trying to update their own password
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        
        // Allow if it's the user's own account or if user is admin
        boolean isAdminUser = user.isAdmin();
        boolean isOwnAccount = user.getId().equals(id);
        
        if (!isOwnAccount && !isAdminUser) {
            return new ResponseEntity<>("Not authorized to update this user's password", HttpStatus.FORBIDDEN);
        }
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return new ResponseEntity<>("Current password and new password are required", HttpStatus.BAD_REQUEST);
        }
        
        // Skip current password check for admins updating other users
        boolean isUpdated;
        if (isAdminUser && !isOwnAccount) {
            // Admin changing someone else's password
            isUpdated = userService.resetPassword(id, newPassword);
        } else {
            // User changing own password
            isUpdated = userService.updatePassword(id, currentPassword, newPassword);
        }
        
        if (isUpdated) {
            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to update password. Current password may be incorrect.", HttpStatus.BAD_REQUEST);
        }
    }

    /**
    * Check password status endpoint
    */
    @GetMapping("/password/status")
    public ResponseEntity<?> checkPasswordStatus(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
    
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    
        Map<String, Object> response = new HashMap<>();
        response.put("isOAuthUser", user.isOAuthUser());
        response.put("hasSetPassword", user.getPasswordSetDate() != null);
        response.put("oauthProvider", user.getOauthProvider());
    
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Create/set password for OAuth users endpoint
    */
    @PostMapping("/password/create")
    public ResponseEntity<?> createPassword(
        @RequestBody Map<String, String> passwordData,
        Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
    
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
    
        if (newPassword == null || newPassword.isEmpty()) {
            return new ResponseEntity<>("New password is required", HttpStatus.BAD_REQUEST);
        }
    
        // Check if user is OAuth user and hasn't set a password yet
        boolean skipCurrentPasswordCheck = user.isOAuthUser() && user.getPasswordSetDate() == null;
    
        // For regular users or OAuth users who have already set a password,
        // validate the current password
        if (!skipCurrentPasswordCheck) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                return new ResponseEntity<>("Current password is required", HttpStatus.BAD_REQUEST);
            }
        
            boolean passwordMatches = userService.checkPassword(user.getId(), currentPassword);
            if (!passwordMatches) {
                return new ResponseEntity<>("Current password is incorrect", HttpStatus.BAD_REQUEST);
            }
        }
    
        // Update password and passwordSetDate
        boolean updated = userService.setPassword(user.getId(), newPassword);
    
        if (updated) {
            return new ResponseEntity<>("Password set successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to set password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
    * Request password reset endpoint
    */
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
    
        if (email == null || email.isEmpty()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }
    
        // Process the reset request
        // Note: For security, always return the same response regardless of whether the email exists
        boolean processed = userService.requestPasswordReset(email);
    
        return new ResponseEntity<>("If your email is registered, you will receive password reset instructions", HttpStatus.OK);
    }

/**
 * Confirm password reset endpoint
 */
@PostMapping("/password/reset-confirm")
public ResponseEntity<?> confirmPasswordReset(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    String newPassword = request.get("newPassword");
    
    if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
        return new ResponseEntity<>("Token and new password are required", HttpStatus.BAD_REQUEST);
    }
    
    boolean reset = userService.confirmPasswordReset(token, newPassword);
    
    if (reset) {
        return new ResponseEntity<>("Password has been reset successfully", HttpStatus.OK);
    } else {
        return new ResponseEntity<>("Invalid or expired token", HttpStatus.BAD_REQUEST);
    }
}

    // Search users by username
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            List<User> users = userService.searchUsersByUsername(query);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to search users: " + e.getMessage());
        }
    }

    
    // Delete user - admins or own user only
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Principal principal) {
        // Check permissions
        User currentUser = userService.getUserByUsername(principal.getName());
        if (currentUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        boolean isAdmin = currentUser.isAdmin();
        boolean isOwnAccount = currentUser.getId().equals(id);
        
        if (!isAdmin && !isOwnAccount) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        
        if (userService.deleteUser(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Get all usernames
    @GetMapping("/allUsernames")
    public ResponseEntity<?> getAllUsernames() {
        try {
            List<String> usernames = userService.getAllUsernames();
            return ResponseEntity.ok(usernames);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch usernames: " + e.getMessage());
        }
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userService.getUserByUsername(principal.getName());
        if (currentUser == null) {
            return new ResponseEntity<>("Authenticated user not found", HttpStatus.UNAUTHORIZED);
        }

        // Allow access to the requested user
        User requestedUser = userService.getUserById(id);
        if (requestedUser == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(requestedUser);
    }
}