package com.cts.ecommerce.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.ecommerce.dto.AuthResponse;
import com.cts.ecommerce.dto.LoginRequest;
import com.cts.ecommerce.dto.RegisterRequest;
import com.cts.ecommerce.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // User registration
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    // User login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(req);
        
        // Put JWT in HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(1 * 60 * 60) // 1 hour
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    // Create session for Thymeleaf after JWT authentication
    @PostMapping("/create-session")
    public ResponseEntity<?> createSession(@RequestHeader("Authorization") String authHeader,
                                           jakarta.servlet.http.HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            // Create session through service
            authService.createSession(token);
            
            // Ensure HTTP session exists and persist SecurityContext so Thymeleaf sees it immediately
            jakarta.servlet.http.HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", org.springframework.security.core.context.SecurityContextHolder.getContext());
            
            return ResponseEntity.ok().body("Session created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Failed to create session");
        }
    }

    // Logout endpoint to clear JWT token
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            // Clear JWT cookie
            ResponseCookie cookie = ResponseCookie.from("jwt", "")
                    .httpOnly(true)
                    .path("/")
                    .maxAge(0) // Expire immediately
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            
            // Clear security context through service
            authService.logout();
            
            return ResponseEntity.ok().body("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Logout failed");
        }
    }

}
