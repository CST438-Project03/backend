package com.example.proj3.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey secretKey;

    private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    public void init() {
        if(secretKeyString == null || secretKeyString.isEmpty()) {
            secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            System.out.println("Secret key generated: " + secretKey.toString());
        } else {
            secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public void invalidateToken(String token) {
        blacklistedTokens.add(token);
    }

    //need to check out what the line does 
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // claims.put("isAdmin", userDetails.isEnabled());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && !isTokenBlacklisted(token));
    }

    @Scheduled(fixedRate = 60000) // 1 minute
    public void clearExpiredTokens() {
        Date now = new Date();
        Set<String> expiredTokens = new HashSet<>();

        for (String token : blacklistedTokens) {
            try {
                Date expiration = extractExpiration(token);
                if (expiration.before(now)) {
                    expiredTokens.add(token);
                }
            } catch (Exception e) {
                expiredTokens.add(token); // If token is invalid, consider it expired
            }
        }

        if(!expiredTokens.isEmpty()) {
            blacklistedTokens.removeAll(expiredTokens);
        }
    }

}