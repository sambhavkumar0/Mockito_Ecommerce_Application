package com.cts.ecommerce.service;

import com.cts.ecommerce.model.Role;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("+1234567890");
        user.setActive(true);
        user.setRoles(java.util.Set.of(Role.ROLE_USER));
    }

    @Test
    void createUserWithRole_createsUser() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User result = userService.createUserWithRole(
            "john", "john@example.com", "password123", 
            "John", "Doe", "+1234567890", Role.ROLE_USER);

        // Then
        assertThat(result.getUsername()).isEqualTo("john");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(result.getRoles()).contains(Role.ROLE_USER);
        assertThat(result.isActive()).isTrue();
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findByEmail_returnsUser() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        User result = userService.findByEmail("john@example.com");

        // Then
        assertThat(result).isEqualTo(user);
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void findByEmail_notFound_throwsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.findByEmail("nonexistent@example.com"));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with email: nonexistent@example.com");
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void existsByEmail_returnsTrue() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("john@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    void existsByEmail_returnsFalse() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("nonexistent@example.com");
    }

    @Test
    void existsByUsername_returnsTrue() {
        // Given
        when(userRepository.existsByUsername("john")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("john");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername("john");
    }

    @Test
    void existsByUsername_returnsFalse() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean result = userService.existsByUsername("nonexistent");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByUsername("nonexistent");
    }

    @Test
    void isUserActive_returnsTrue() {
        // Given
        user.setActive(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        boolean result = userService.isUserActive("john@example.com");

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void isUserActive_returnsFalse() {
        // Given
        user.setActive(false);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        boolean result = userService.isUserActive("john@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void isUserActive_notFound_returnsFalse() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        boolean result = userService.isUserActive("nonexistent@example.com");

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        // Given
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setRoles(java.util.Set.of(Role.ROLE_ADMIN));
        
        List<User> users = Arrays.asList(user, admin);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(user, admin);
        verify(userRepository).findAll();
    }

    @Test
    void getActiveUsers_returnsActiveUsersOnly() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setId(2L);
        inactiveUser.setUsername("inactive");
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setActive(false);
        
        List<User> allUsers = Arrays.asList(user, inactiveUser);
        when(userRepository.findAll()).thenReturn(allUsers);

        // When
        List<User> result = userService.getActiveUsers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(user);
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_deletesUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.deleteUser(999L));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 999");
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void toggleUserStatus_activatesInactiveUser() {
        // Given
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.toggleUserStatus(1L);

        // Then
        assertThat(user.isActive()).isTrue();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserStatus_deactivatesActiveUser() {
        // Given
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.toggleUserStatus(1L);

        // Then
        assertThat(user.isActive()).isFalse();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void toggleUserStatus_notFound_throwsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.toggleUserStatus(999L));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 999");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUser_setsActiveTrue() {
        // Given
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.activateUser(1L);

        // Then
        assertThat(user.isActive()).isTrue();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_notFound_throwsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.activateUser(999L));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 999");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_setsActiveFalse() {
        // Given
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        userService.deactivateUser(1L);

        // Then
        assertThat(user.isActive()).isFalse();
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_notFound_throwsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.deactivateUser(999L));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 999");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_updatesUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("john_updated");
        updatedUser.setEmail("john@example.com");
        updatedUser.setFirstName("John Updated");
        updatedUser.setLastName("Doe Updated");
        updatedUser.setPhoneNumber("+9876543210");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);

        // When
        User result = userService.updateUser(updatedUser);

        // Then
        assertThat(result.getUsername()).isEqualTo("john_updated");
        assertThat(result.getFirstName()).isEqualTo("John Updated");
        assertThat(result.getLastName()).isEqualTo("Doe Updated");
        assertThat(result.getPhoneNumber()).isEqualTo("+9876543210");
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throwsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.updateUser(user));
        
        assertThat(exception.getMessage()).isEqualTo("User not found with id: 999");
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }
}
