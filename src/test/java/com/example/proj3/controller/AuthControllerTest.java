package com.example.proj3.controller;

import com.example.proj3.config.JwtUtil;
import com.example.proj3.model.User;
import com.example.proj3.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testRegisterUser_Success() throws Exception {
        Mockito.when(userService.userExistsByUsername("johndoe")).thenReturn(false);
        Mockito.when(userService.userExistsByEmail("john@example.com")).thenReturn(false);
        Mockito.when(userService.createUser(any(User.class))).thenReturn(true);

        String json = """
            {
              "username": "johndoe",
              "email": "john@example.com",
              "password": "secret123"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Registration successful! Please login."));
    }

    @Test
    void testLogin_Success() throws Exception {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        UserDetails mockUserDetails = Mockito.mock(UserDetails.class);
        User mockUser = new User();
        mockUser.setId(123L);
        mockUser.setUsername("johndoe");

        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        Mockito.when(mockAuth.getPrincipal()).thenReturn(mockUserDetails);
        Mockito.when(mockUserDetails.getUsername()).thenReturn("johndoe");
        Mockito.when(jwtUtil.generateToken(any())).thenReturn("fake-jwt-token");
        Mockito.when(userService.getUserByUsername("johndoe")).thenReturn(mockUser);

        mockMvc.perform(post("/auth/login")
                .param("username", "johndoe")
                .param("password", "secret123"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jwtToken").value("fake-jwt-token"))
            .andExpect(jsonPath("$.username").value("johndoe"))
            .andExpect(jsonPath("$.userId").value(123));
    }

    @Test
    void testRegisterUser_MissingFields() throws Exception {
        String json = """
            {
              "username": "johndoe"
            }
        """;

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Username, email, and password are required"));
    }

    @Test
    void testSignup_Success() throws Exception {
        Mockito.when(userService.getUserByUsername("newuser")).thenReturn(null);
        Mockito.when(userService.getUserByEmail("new@user.com")).thenReturn(null);
        Mockito.when(userService.createUser(any(User.class))).thenReturn(true);

        User createdUser = new User();
        createdUser.setId(42L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@user.com");
        createdUser.setAdmin(false);

        Mockito.when(userService.getUserByUsername("newuser")).thenReturn(createdUser);

        mockMvc.perform(post("/auth/signup")
                .param("username", "newuser")
                .param("email", "new@user.com")
                .param("password", "securepass"))
            .andExpect(status().isOk())
            .andExpect(content().string("User has been created!"));
    }

    @Test
    void testSignup_UserAlreadyExists() throws Exception {
        Mockito.when(userService.getUserByUsername("existinguser")).thenReturn(new User());

        mockMvc.perform(post("/auth/signup")
                .param("username", "existinguser")
                .param("email", "exist@user.com")
                .param("password", "abc123"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("User Already Exists!"));
    }

    @Test
    void testOAuthRedirect() throws Exception {
        mockMvc.perform(get("/auth/oauth2/redirect")
                .param("token", "abc.def.ghi"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("abc.def.ghi"))
            .andExpect(jsonPath("$.message").value("Successfully authenticated via OAuth2"));
    }

    @Test
    void testOAuthError() throws Exception {
        mockMvc.perform(get("/auth/oauth2/error")
                .param("error", "access_denied"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", "Bearer faketoken123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Successfully logged out!"));

        Mockito.verify(jwtUtil).invalidateToken("faketoken123");
    }
}
