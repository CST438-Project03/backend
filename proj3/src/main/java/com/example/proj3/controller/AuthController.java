package com.example.proj3.controller;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.repository.UserRepository;
import com.example.proj3.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    
  @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        logger.info("User attempting to login: " + username);
        try {
            logger.info("Attempting to authenticate user: " + username);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            logger.info("User has been authenticated: " + username);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.info("User details: " + userDetails);

            String jwtToken = jwtUtil.generateToken(userDetails);
            logger.info("JWT Token generated: " + jwtToken);

            // Get the user ID
            User user;
            user = userService.getUserByUsername(username);
            Long userId = user.getId();

            Map<String, Object> response = new HashMap<>();
            response.put("jwtToken", jwtToken);
            response.put("username", userDetails.getUsername());
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {

            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during login: " + e.getMessage());
            return ResponseEntity.status(500).body("Error during login: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestParam String username, @RequestParam String email, @RequestParam String password){
        if(userService.getUserByUsername(username) != null || userService.getUserByEmail(email) !=null) {
            return ResponseEntity.badRequest().body("User Already Exists!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setAdmin(false); // need to change this to reflect how we actually set role

        boolean success = userService.createUser(user);

        if(success) {
             User createdUser = userService.getUserByUsername(username);
              Map<String, Object> response = new HashMap<>();
            response.put("id", createdUser.getId());
            response.put("username", createdUser.getUsername());
            response.put("email", createdUser.getEmail());
            response.put("isAdmin", createdUser.isAdmin());
            response.put("message", "User has been created successfully!");
            return ResponseEntity.ok("User has been created!");
        }else {
            return ResponseEntity.internalServerError().body("Unable to create user.");

        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Invalidate the token
            jwtUtil.invalidateToken(token);
            }
            
        // Clear security context
        SecurityContextHolder.clearContext();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Successfully logged out!");
            
        return ResponseEntity.ok(response);
    }


    
    
}