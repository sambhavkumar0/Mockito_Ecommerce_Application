package com.cts.ecommerce.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.UserRepository;
import com.cts.ecommerce.dto.ProfileUpdateRequest;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        // Encode password if not already encoded
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Validates user registration data
     * @param username Username to validate
     * @param email Email to validate
     * @return ValidationResult with validation status and error message
     */
    public ValidationResult validateUserRegistration(String username, String email) {
        if (existsByUsername(username)) {
            return new ValidationResult(false, "Username already taken");
        }
        
        if (existsByEmail(email)) {
            return new ValidationResult(false, "Email already registered");
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates password confirmation
     * @param password Original password
     * @param confirmPassword Confirmation password
     * @return true if passwords match, false otherwise
     */
    public boolean validatePasswordConfirmation(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Creates a user with validation
     * @param user User to create
     * @param confirmPassword Password confirmation
     * @return ValidationResult with creation status and error message
     */
    public ValidationResult createUserWithValidation(User user, String confirmPassword) {
        // Validate password confirmation
        if (!validatePasswordConfirmation(user.getPassword(), confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }
        
        // Validate username and email uniqueness
        ValidationResult validationResult = validateUserRegistration(user.getUsername(), user.getEmail());
        if (!validationResult.isValid()) {
            return validationResult;
        }
        
        // Set default role and create user
        user.setRoles(Set.of(Role.ROLE_USER));
        createUser(user);
        
        return new ValidationResult(true, "User registered successfully! Please login.");
    }

    /**
     * Inner class to hold validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User createUserWithRole(String username, String email, String password, String firstName, 
                                   String lastName, String phoneNumber, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setRoles(Set.of(role));
        
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // User status management methods
    @Transactional
    public void activateUser(Long userId) {
        User user = findById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByActiveFalse();
    }

    public boolean isUserActive(String email) {
        return userRepository.findByEmail(email)
                .map(User::isActive)
                .orElse(false);
    }

    /**
     * Updates user profile information
     * @param email Current user's email
     * @param request Profile update request
     * @return ValidationResult with update status and error message
     */
    @Transactional
    public ValidationResult updateUserProfile(String email, ProfileUpdateRequest request) {
        try {
            User user = findByEmail(email);
            
            // Check if email is being changed and if it already exists
            if (!email.equals(request.getEmail()) && existsByEmail(request.getEmail())) {
                return new ValidationResult(false, "Email already exists. Please choose a different email.");
            }
            
            // Update user information
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            userRepository.save(user);
            
            return new ValidationResult(true, "Profile updated successfully!");
        } catch (EntityNotFoundException e) {
            return new ValidationResult(false, "User not found.");
        } catch (Exception e) {
            return new ValidationResult(false, "Failed to update profile. Please try again.");
        }
    }

    /**
     * Gets user profile information by email
     * @param email User's email
     * @return User object
     * @throws EntityNotFoundException if user not found
     */
    public User getUserProfile(String email) {
        return findByEmail(email);
    }
}
