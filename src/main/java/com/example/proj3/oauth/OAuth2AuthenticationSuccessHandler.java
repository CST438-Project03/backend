package com.example.proj3.oauth;

import java.io.IOException;
import java.security.SecureRandom;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil, UserService userService, 
                                            UserDetailsService userDetailsService,
                                            PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
        System.out.println("OAuth authentication success handler triggered!");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Query String: " + request.getQueryString());
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            System.out.println("OAuth2User attributes: " + oAuth2User.getAttributes());
            
            // Extract user information
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            System.out.println("Email: " + email + ", Name: " + name);
            
            // Check if user exists
            User user = userService.getUserByEmail(email);
            System.out.println("User exists in database: " + (user != null));
            
            if (user == null) {
                // Create new user if they don't exist
                System.out.println("Creating new user for: " + email);
                user = new User();
                user.setEmail(email);
                
                // Create a username based on email
                String username = email.split("@")[0];
                if (userService.userExistsByUsername(username)) {
                    username = username + "-" + System.currentTimeMillis() % 10000;
                }
                System.out.println("Generated username: " + username);
                
                user.setUsername(username);
                
                // Generate a random secure password and encode it
                String randomPassword = generateSecureRandomPassword();
                user.setPassword(passwordEncoder.encode(randomPassword));
                
                user.setOAuthUser(true); 
                user.setOauthProvider("google"); 
                user.setAdmin(false);
                
                boolean created = userService.createUser(user);
                System.out.println("User creation success: " + created);
                
                // Get the user again to ensure we have the ID
                user = userService.getUserByEmail(email);
                System.out.println("Retrieved user after creation: " + (user != null));
            }
            
            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            System.out.println("UserDetails loaded for: " + user.getUsername());
            
            String token = jwtUtil.generateToken(userDetails);
            System.out.println("JWT token generated successfully");
            
            // Redirect to frontend with token
            String frontendRedirectUrl = "https://frontend-pi-nine-14.vercel.app/oauth-callback?token=" + token +
                                        "&userId=" + user.getId() + 
                                        "&username=" + user.getUsername();
            
            System.out.println("Redirecting to: " + frontendRedirectUrl);
            getRedirectStrategy().sendRedirect(request, response, frontendRedirectUrl);
            System.out.println("Redirect executed");
            
        } catch (Exception e) {
            System.err.println("Error in OAuth success handler: " + e.getMessage());
            e.printStackTrace();
            
            // Redirect to login page with error
            getRedirectStrategy().sendRedirect(request, response, "https://frontend-pi-nine-14.vercel.app/login?error=oauth_failed");
        }
    }
    
    private String generateSecureRandomPassword() {
        // Generate a random 16-character password with letters, numbers, and special characters
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        
        for (int i = 0; i < 16; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        
        return sb.toString();
    }
}