package com.example.proj3.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, message = "Password should be at least 6 characters long")
    private String password;

    @NotNull(message = "Role is required")
    @Column(name = "is_admin", nullable = false)
    boolean admin;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(nullable = false)
    private boolean isOAuthUser = false;
    
    @Column(nullable = true)
    private String oauthProvider;
    
    @Column(nullable = true)
    private String passwordSetDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean isAdmin) {
        this.admin = admin;
    }
    
    public String getProfilePicture() {
        return profilePicture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

        public boolean isOAuthUser() {
        return isOAuthUser;
    }
    
    public void setOAuthUser(boolean isOAuthUser) {
        this.isOAuthUser = isOAuthUser;
    }
    
    public String getOauthProvider() {
        return oauthProvider;
    }
    
    public void setOauthProvider(String oauthProvider) {
        this.oauthProvider = oauthProvider;
    }
    
    public String getPasswordSetDate() {
        return passwordSetDate;
    }
    
    public void setPasswordSetDate(String passwordSetDate) {
        this.passwordSetDate = passwordSetDate;
    }
    
    public boolean hasSetPassword() {
        return passwordSetDate != null;
    }
}