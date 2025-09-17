package com.cts.ecommerce.controller;

import com.cts.ecommerce.dto.AuthResponse;
import com.cts.ecommerce.dto.LoginRequest;
import com.cts.ecommerce.dto.RegisterRequest;
import com.cts.ecommerce.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock private AuthService authService;
    @Mock private HttpServletResponse response;
    @Mock private HttpServletRequest request;
    @Mock private HttpSession session;

    @InjectMocks private AuthController controller;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setPassword("secret");
        registerRequest.setConfirmPassword("secret");
        registerRequest.setEmail("john@example.com");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("secret");

        authResponse = new AuthResponse("token123", "john");
    }

    @Test
    void register_returnsAuthResponse() {
        // Given
        when(authService.register(registerRequest)).thenReturn(authResponse);

        // When
        AuthResponse result = controller.register(registerRequest);

        // Then
        assertThat(result).isEqualTo(authResponse);
        assertThat(result.getToken()).isEqualTo("token123");
        assertThat(result.getUsername()).isEqualTo("john");
        verify(authService).register(registerRequest);
    }

    @Test
    void register_withInvalidData_throwsException() {
        // Given
        registerRequest.setConfirmPassword("different");
        when(authService.register(registerRequest))
            .thenThrow(new RuntimeException("Passwords do not match"));

        // When & Then
        try {
            controller.register(registerRequest);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Passwords do not match");
        }
        verify(authService).register(registerRequest);
    }

    @Test
    void login_returnsResponseEntityWithCookie() {
        // Given
        when(authService.login(loginRequest)).thenReturn(authResponse);

        // When
        ResponseEntity<?> result = controller.login(loginRequest, response);

        // Then
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo(authResponse);
        verify(authService).login(loginRequest);
        verify(response).addHeader("Set-Cookie", anyString());
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        // Given
        when(authService.login(loginRequest))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        try {
            controller.login(loginRequest, response);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid credentials");
        }
        verify(authService).login(loginRequest);
    }

    @Test
    void createSession_success() {
        // Given
        String token = "valid-token";
        String authHeader = "Bearer " + token;
        UserDetails userDetails = new User("john@example.com", "password", Collections.emptyList());
        
        when(request.getSession(true)).thenReturn(session);
        when(authService.createSession(token)).thenReturn(userDetails);

        // When
        ResponseEntity<?> result = controller.createSession(authHeader, request);

        // Then
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo("Session created successfully");
        verify(authService).createSession(token);
        verify(session).setAttribute("SPRING_SECURITY_CONTEXT", any());
    }

    @Test
    void createSession_invalidToken() {
        // Given
        String token = "invalid-token";
        String authHeader = "Bearer " + token;
        
        when(authService.createSession(token))
            .thenThrow(new RuntimeException("Invalid token"));

        // When
        ResponseEntity<?> result = controller.createSession(authHeader, request);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(401);
        assertThat(result.getBody()).isEqualTo("Failed to create session");
        verify(authService).createSession(token);
    }

    @Test
    void createSession_withoutBearerPrefix() {
        // Given
        String authHeader = "invalid-token";

        // When
        ResponseEntity<?> result = controller.createSession(authHeader, request);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(401);
        assertThat(result.getBody()).isEqualTo("Failed to create session");
    }

    @Test
    void logout_success() {
        // Given - no setup needed for successful logout

        // When
        ResponseEntity<?> result = controller.logout(response);

        // Then
        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isEqualTo("Logged out successfully");
        verify(authService).logout();
        verify(response).addHeader("Set-Cookie", anyString());
    }

    @Test
    void logout_withException() {
        // Given
        doThrow(new RuntimeException("Logout failed")).when(authService).logout();

        // When
        ResponseEntity<?> result = controller.logout(response);

        // Then
        assertThat(result.getStatusCode().value()).isEqualTo(500);
        assertThat(result.getBody()).isEqualTo("Logout failed");
        verify(authService).logout();
    }
}
