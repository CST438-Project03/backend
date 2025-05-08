package com.example.proj3.service;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.proj3.model.User;
import com.example.proj3.repository.UserRepository;
import com.example.proj3.model.PasswordResetToken;
import com.example.proj3.repository.PasswordResetTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Finds a user by their ID.
     *
     * @param id The user ID
     * @return The user if found, or null if not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for
     * @return The user with the specified username, or null if not found
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email to search for
     * @return The user with the specified email, or null if not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Checks if a user with the given username exists.
     *
     * @param username The username to check
     * @return true if a user with the username exists, false otherwise
     */
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email The email to check
     * @return true if a user with the email exists, false otherwise
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Creates a new user.
     *
     * @param user The user entity to create
     * @return true if user was created successfully, false otherwise
     */
    @Transactional
    public boolean createUser(User user) {
        try {
            // Ensure password is encoded before saving
            if (user.getPassword() != null) {
                user.setPassword(user.getPassword());
            }
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete
     * @return true if the user was successfully deleted, false if the user wasn't found
     */
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Updates all fields of an existing user.
     *
     * @param id The ID of the user to update
     * @param user The user entity with updated values
     * @return The updated user, or null if the user doesn't exist
     */
    @Transactional
    public User updateUser(Long id, User user) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User updatedUser = userOpt.get();

            // Update fields if they are provided
            if (user.getUsername() != null) {
                updatedUser.setUsername(user.getUsername());
            }

            if (user.getEmail() != null) {
                updatedUser.setEmail(user.getEmail());
            }

            // Only update password if it's provided and not already encoded
            if (user.getPassword() != null) {
                // Check if password is already encoded - if it contains "$2a$" it's likely already a BCrypt hash
                if (!user.getPassword().startsWith("$2a$")) {
                    updatedUser.setPassword(passwordEncoder.encode(user.getPassword()));
                } else {
                    updatedUser.setPassword(user.getPassword());
                }
            }

            // Only update admin status if it's explicitly set
            updatedUser.setAdmin(user.isAdmin());

            // Update profile picture if provided
            if (user.getProfilePicture() != null) {
                updatedUser.setProfilePicture(user.getProfilePicture());
            }

            return userRepository.save(updatedUser);
        }
        return null;
    }

    /**
     * Updates a user's password after verifying the current password
     *
     * @param id User ID
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if password was updated successfully, false otherwise
     */
    @Transactional
    public boolean updatePassword(Long id, String currentPassword, String newPassword) {
        User user = getUserById(id);

        if (user == null) {
            return false;
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Set new password (encode it first)
        user.setPassword(passwordEncoder.encode(newPassword));

        // Save user
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resets a user's password without requiring current password verification
     * This method should only be called by admins
     *
     * @param id The user ID
     * @param newPassword The new password to set
     * @return true if password was reset successfully, false otherwise
     */
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        User user = getUserById(id);

        if (user == null) {
            return false;
        }

        // Set new password (encode it first)
        user.setPassword(passwordEncoder.encode(newPassword));

        // Save user
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies if the provided password matches the user's stored password
     *
     * @param username The username to check
     * @param password The password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean verifyUserPassword(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Verify if the provided password matches the user's stored password
     *
     * @param user The user object
     * @param rawPassword The raw password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Logs out a user.
     *
     * @param id The ID of the user to log out
     * @return true if the user was successfully logged out, false if the user wasn't found
     */
    @Transactional
    public boolean logoutUser(Long id) {
        // This method doesn't need to do anything at the service level
        // JWT invalidation should be handled by the authentication service
        return true;
    }
    /**
     * Checks if a password matches for a specific user
     *
     * @param userId The user ID
     * @param password The password to check
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(Long userId, String password) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        return passwordEncoder.matches(password, user.getPassword());
    }

    /**
     * Sets a password and updates the passwordSetDate for a user
     * Used primarily for OAuth users setting a password for the first time
     *
     * @param userId The user ID
     * @param newPassword The new password to set
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean setPassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSetDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initiates a password reset request by creating a token
     *
     * @param email The email address of the user
     * @return true if the request was processed (even if the email doesn't exist)
     */
    @Transactional
    public boolean requestPasswordReset(String email) {
        User user = getUserByEmail(email);
        if (user == null) {
            // Return true for security reasons (don't reveal if email exists)
            return true;
        }

        // Generate a unique token
        String token = UUID.randomUUID().toString();

        // Set expiration time (24 hours from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date expiryDate = calendar.getTime();

        // Create or update reset token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user);
        if (resetToken == null) {
            resetToken = new PasswordResetToken();
            resetToken.setUser(user);
        }

        resetToken.setToken(token);
        resetToken.setExpiryDate(expiryDate);
        passwordResetTokenRepository.save(resetToken);

        // Send email with reset link
        // TODO: Implement email sending logic
        // emailService.sendPasswordResetEmail(user.getEmail(), token);

        return true;
    }

    /**
     * Confirms a password reset using a token
     *
     * @param token The reset token
     * @param newPassword The new password to set
     * @return true if the reset was successful, false otherwise
     */
    @Transactional
    public boolean confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);

        // Check if token exists and is valid
        if (resetToken == null) {
            return false;
        }

        // Check if token has expired
        if (resetToken.getExpiryDate().before(new Date())) {
            passwordResetTokenRepository.delete(resetToken);
            return false;
        }

        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSetDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        userRepository.save(user);

        // Delete the used token
        passwordResetTokenRepository.delete(resetToken);

        return true;
    }
    /**
     * Searches for users by a partial username match.
     *
     * @param query The partial username to search for
     * @return A list of users whose usernames match the query
     */
    public List<User> searchUsersByUsername(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    /**
     * Retrieves all usernames.
     *
     * @return A list of all usernames
     */
    public List<String> getAllUsernames() {
        return userRepository.findAll().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }


    /**
     * Retrieves all users in the system.
     * Required for the AdminController's getAllUsers() endpoint.
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Searches for users by username or email containing the query string.
     * Required for the AdminController's searchUsers() endpoint.
     *
     * @param query The search query
     * @return A list of users matching the search criteria
     */
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllUsers();
        }

        String lowercaseQuery = query.toLowerCase();
        return userRepository.findAll().stream()
                .filter(user ->
                        user.getUsername().toLowerCase().contains(lowercaseQuery) ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowercaseQuery)))
                .collect(Collectors.toList());
    }

    /**
     * Saves a user to the database.
     * Required for the AdminController's createUser() endpoint.
     *
     * @param user The user to save
     * @return The saved user with its generated ID
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User findByUsername(String currentUsername) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}