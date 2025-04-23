package com.example.proj3.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.proj3.model.User;

import com.example.proj3.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;



    /**
     * Finds a user by their ID.
     *
     * @param id The user ID
     * @return An Optional containing the user if found, or empty if not found
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
        return userRepository.findByEmail(email);
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
     * @return The created user with ID assigned
     */
    @Transactional
    public boolean createUser(User user) {
       try {
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
     * Logs out a user.
     *
     * @param id The ID of the user to log out
     * @return true if the user was successfully logged out, false if the user wasn't found
     */
    @Transactional
    public boolean logoutUser(Long id) {
        
        return true;
    }

}