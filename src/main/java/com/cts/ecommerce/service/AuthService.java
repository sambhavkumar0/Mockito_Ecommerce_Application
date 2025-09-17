package com.cts.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.ecommerce.dto.AuthResponse;
import com.cts.ecommerce.dto.LoginRequest;
import com.cts.ecommerce.dto.RegisterRequest;
import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Validate username uniqueness
        if (userService.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Validate email uniqueness
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        
        // Validate password confirmation
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Create user with USER role
        User user = userService.createUserWithRole(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            registerRequest.getPassword(),
            registerRequest.getFirstName(),
            registerRequest.getLastName(),
            registerRequest.getPhoneNumber(),
            Role.ROLE_USER
        );

        // Authenticate newly registered user immediately
        Authentication auth = new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getUsername());
    }

    public AuthResponse login(LoginRequest loginRequest) {
        // Check if user is active before attempting authentication
        if (!userService.isUserActive(loginRequest.getEmail())) {
            throw new RuntimeException("Account is inactive. Please contact administrator.");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Get the user to return username in response
        User user = userService.findByEmail(loginRequest.getEmail());
        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getUsername());
    }

    public UserDetails createSession(String token) {
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            // Create authentication object and set it in SecurityContext
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return userDetails;
        } else {
            throw new RuntimeException("Invalid token");
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
