package com.cts.ecommerce.controller;

import com.cts.ecommerce.model.User;
import com.cts.ecommerce.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerUnitTest {

    @Mock private UserService userService;
    @InjectMocks private LoginController controller;

    private Model model;
    private User user;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        
        user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("+1234567890");
    }

    @Test
    void showRegisterForm_returnsRegisterViewWithUserModel() {
        // When
        String view = controller.showRegisterForm(model);

        // Then
        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("user")).isInstanceOf(User.class);
    }

    @Test
    void registerUser_withValidData_returnsLoginView() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUserWithValidation(any(User.class), any(String.class)))
            .thenReturn(new UserService.ValidationResult(true, "User registered successfully"));

        // When
        String view = controller.registerUser(user, bindingResult, "password123", model);

        // Then
        assertThat(view).isEqualTo("login");
        assertThat(model.getAttribute("message")).isEqualTo("User registered successfully");
        verify(userService).createUserWithValidation(user, "password123");
    }

    @Test
    void registerUser_withValidationErrors_returnsRegisterView() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        String view = controller.registerUser(user, bindingResult, "password123", model);

        // Then
        assertThat(view).isEqualTo("register");
        verify(userService, never()).createUserWithValidation(any(User.class), any(String.class));
    }

    @Test
    void registerUser_withInvalidValidation_returnsRegisterViewWithError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUserWithValidation(any(User.class), any(String.class)))
            .thenReturn(new UserService.ValidationResult(false, "Username already taken"));

        // When
        String view = controller.registerUser(user, bindingResult, "password123", model);

        // Then
        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("Username already taken");
        verify(userService).createUserWithValidation(user, "password123");
    }

    @Test
    void registerUser_withException_returnsRegisterViewWithError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.createUserWithValidation(any(User.class), any(String.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When
        String view = controller.registerUser(user, bindingResult, "password123", model);

        // Then
        assertThat(view).isEqualTo("register");
        assertThat(model.getAttribute("error")).isEqualTo("Registration failed. Please try again.");
        verify(userService).createUserWithValidation(user, "password123");
    }

    @Test
    void showLoginForm_returnsLoginView() {
        // When
        String view = controller.showLoginForm(model);

        // Then
        assertThat(view).isEqualTo("login");
    }
}
