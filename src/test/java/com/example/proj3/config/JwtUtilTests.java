// package com.example.proj3.config;

// import io.jsonwebtoken.Claims;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.test.util.ReflectionTestUtils;

// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.when;

// @SpringBootTest
// public class JwtUtilTests {

//     private JwtUtil jwtUtil;

//     @Mock
//     private UserDetails userDetails;

//     private final String SECRET_KEY = "testSecretKeyWithAtLeast32Characters1234567890";
//     private final long EXPIRATION_TIME = 3600000; // 1 hour

//     @BeforeEach
//     void setUp() {
//         jwtUtil = new JwtUtil();
//         ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET_KEY);
//         ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", EXPIRATION_TIME);

//         when(userDetails.getUsername()).thenReturn("testuser");
//     }

//     @Test
//     void generateToken_Success() {
//         // Act
//         String token = jwtUtil.generateToken(userDetails);

//         // Assert
//         assertNotNull(token);
//         assertTrue(token.length() > 0);
//     }

//     @Test
//     void generateToken_WithClaims_Success() {
//         // Arrange
//         Map<String, Object> claims = new HashMap<>();
//         claims.put("role", "USER");
//         claims.put("id", 123);

//         // Act
//         String token = jwtUtil.generateToken(claims, userDetails);

//         // Assert
//         assertNotNull(token);
//         assertTrue(token.length() > 0);
//     }

//     @Test
//     void validateToken_Valid() {
//         // Arrange
//         String token = jwtUtil.generateToken(userDetails);

//         // Act
//         boolean isValid = jwtUtil.validateToken(token, userDetails);

//         // Assert
//         assertTrue(isValid);
//     }

//     @Test
//     void validateToken_InvalidUsername() {
//         // Arrange
//         String token = jwtUtil.generateToken(userDetails);
//         UserDetails differentUser = org.springframework.security.core.userdetails.User
//                 .withUsername("otheruser")
//                 .password("password")
//                 .authorities("USER")
//                 .build();

//         // Act
//         boolean isValid = jwtUtil.validateToken(token, differentUser);

//         // Assert
//         assertFalse(isValid);
//     }

//     @Test
//     void validateToken_Expired() {
//         // Arrange - Set expiration to a negative value to create expired token
//         ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", -10000);
//         String token = jwtUtil.generateToken(userDetails);
        
//         // Reset expiration time for next tests
//         ReflectionTestUtils.setField(jwtUtil, "EXPIRATION_TIME", EXPIRATION_TIME);

//         // Act
//         boolean isValid = jwtUtil.validateToken(token, userDetails);

//         // Assert
//         assertFalse(isValid);
//     }

//     @Test
//     void extractUsername_Success() {
//         // Arrange
//         String token = jwtUtil.generateToken(userDetails);

//         // Act
//         String username = jwtUtil.extractUsername(token);

//         // Assert
//         assertEquals("testuser", username);
//     }

//     @Test
//     void extractExpiration_Success() {
//         // Arrange
//         String token = jwtUtil.generateToken(userDetails);

//         // Act
//         Date expiration = jwtUtil.extractExpiration(token);

//         // Assert
//         assertNotNull(expiration);
//         long now = System.currentTimeMillis();
//         assertTrue(expiration.getTime() > now);
//         assertTrue(expiration.getTime() <= now + EXPIRATION_TIME + 1000); // Allow 1 second leeway
//     }

//     @Test
//     void extractAllClaims_Success() {
//         // Arrange
//         Map<String, Object> customClaims = new HashMap<>();
//         customClaims.put("role", "USER");
//         customClaims.put("id", 123);
//         String token = jwtUtil.generateToken(customClaims, userDetails);

//         // Act
//         Claims claims = jwtUtil.extractAllClaims(token);

//         // Assert
//         assertNotNull(claims);
//         assertEquals("testuser", claims.getSubject());
//         assertEquals("USER", claims.get("role"));
//         assertEquals(123, claims.get("id"));
//     }

//     @Test
//     void invalidateToken_Success() {
//         // Arrange
//         String token = jwtUtil.generateToken(userDetails);
        
//         // Act
//         jwtUtil.invalidateToken(token);
        
//         // Assert
//         // If you have implemented a blacklist or token revocation mechanism, validate it here
//         // For example, check that the token has been added to a blacklist
//         boolean isValid = jwtUtil.validateToken(token, userDetails);
//         assertFalse(isValid); // Assuming invalidateToken marks the token as invalid
//     }
// }