package com.example.proj3.oauth;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException exception) throws IOException, ServletException {
        System.err.println("OAuth authentication failure: " + exception.getMessage());
        exception.printStackTrace();
    
        getRedirectStrategy().sendRedirect(request, response, "/oauth2/error?error=" + exception.getMessage());
}
}