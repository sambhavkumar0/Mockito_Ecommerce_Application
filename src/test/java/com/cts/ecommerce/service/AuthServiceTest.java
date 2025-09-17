package com.cts.ecommerce.service;

import com.cts.ecommerce.dto.AuthResponse;
import com.cts.ecommerce.dto.LoginRequest;
import com.cts.ecommerce.dto.RegisterRequest;
import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserService userService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserDetailsServiceImpl userDetailsService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setEmail("john@example.com");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setActive(true);
    }

    @Test
    void register_success() {
        // Given
        when(userService.existsByUsername("john")).thenReturn(false);
        when(userService.existsByEmail("john@example.com")).thenReturn(false);
        when(userService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(Role.class)))
            .thenReturn(user);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        // When
        AuthResponse result = authService.register(registerRequest);

        // Then
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUsername()).isEqualTo("john");
        verify(userService).createUserWithRole("john", "john@example.com", "password123", 
            "John", "Doe", "+1234567890", Role.ROLE_USER);
        verify(jwtUtil).generateToken("john@example.com");
    }

    @Test
    void register_usernameAlreadyTaken_throwsException() {
        // Given
        when(userService.existsByUsername("john")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));
        
        assertThat(exception.getMessage()).isEqualTo("Username already taken");
        verify(userService).existsByUsername("john");
        verify(userService, never()).createUserWithRole(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), any(Role.class));
    }

    @Test
    void register_emailAlreadyRegistered_throwsException() {
        // Given
        when(userService.existsByUsername("john")).thenReturn(false);
        when(userService.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));
        
        assertThat(exception.getMessage()).isEqualTo("Email already registered");
        verify(userService).existsByUsername("john");
        verify(userService).existsByEmail("john@example.com");
        verify(userService, never()).createUserWithRole(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), any(Role.class));
    }

    @Test
    void register_passwordMismatch_throwsException() {
        // Given
        registerRequest.setConfirmPassword("different-password");
        when(userService.existsByUsername("john")).thenReturn(false);
        when(userService.existsByEmail("john@example.com")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));
        
        assertThat(exception.getMessage()).isEqualTo("Passwords do not match");
        verify(userService, never()).createUserWithRole(anyString(), anyString(), anyString(), 
            anyString(), anyString(), anyString(), any(Role.class));
    }

    @Test
    void login_success() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "password123");
        when(userService.isUserActive("john@example.com")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(auth);
        when(userService.findByEmail("john@example.com")).thenReturn(user);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUsername()).isEqualTo("john");
        verify(userService).isUserActive("john@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail("john@example.com");
        verify(jwtUtil).generateToken("john@example.com");
    }

    @Test
    void login_inactiveUser_throwsException() {
        // Given
        when(userService.isUserActive("john@example.com")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.login(loginRequest));
        
        assertThat(exception.getMessage()).isEqualTo("Account is inactive. Please contact administrator.");
        verify(userService).isUserActive("john@example.com");
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_invalidCredentials_throwsException() {
        // Given
        when(userService.isUserActive("john@example.com")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(userService).isUserActive("john@example.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, never()).findByEmail(anyString());
    }

    @Test
    void createSession_success() {
        // Given
        String token = "valid-jwt-token";
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("john@example.com")
            .password("password")
            .authorities(Collections.emptyList())
            .build();
        
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsernameFromToken(token)).thenReturn("john@example.com");
        when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);

        // When
        UserDetails result = authService.createSession(token);

        // Then
        assertThat(result).isEqualTo(userDetails);
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername("john@example.com");
    }

    @Test
    void createSession_invalidToken_throwsException() {
        // Given
        String token = "invalid-jwt-token";
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.createSession(token));
        
        assertThat(exception.getMessage()).isEqualTo("Invalid token");
        verify(jwtUtil).validateToken(token);
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void logout_clearsSecurityContext() {
        // Given - SecurityContext is initially set
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "password"));

        // When
        authService.logout();

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void register_setsAuthenticationContext() {
        // Given
        when(userService.existsByUsername("john")).thenReturn(false);
        when(userService.existsByEmail("john@example.com")).thenReturn(false);
        when(userService.createUserWithRole(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(Role.class)))
            .thenReturn(user);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        // When
        authService.register(registerRequest);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("john@example.com");
        verify(userService).createUserWithRole("john", "john@example.com", "password123", 
            "John", "Doe", "+1234567890", Role.ROLE_USER);
    }

    @Test
    void login_setsAuthenticationContext() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("john@example.com", "password123");
        when(userService.isUserActive("john@example.com")).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(auth);
        when(userService.findByEmail("john@example.com")).thenReturn(user);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        // When
        authService.login(loginRequest);

        // Then
        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(contextAuth).isNotNull();
        assertThat(contextAuth.getName()).isEqualTo("john@example.com");
    }
}
