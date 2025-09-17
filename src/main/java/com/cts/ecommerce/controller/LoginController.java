package com.cts.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import com.cts.ecommerce.model.User;
import com.cts.ecommerce.service.UserService;

@Controller
public class LoginController {

    private final UserService userService;

    // Constructor injection for UserService
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // Display register page with empty User object for Thymeleaf form binding
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User()); // Required for th:object="${user}"
        return "register"; // Thymeleaf template name: register.html
    }

    // Handle registration form submission
    @PostMapping(value = "/register", consumes = "application/x-www-form-urlencoded")
    public String registerUser(@Valid @ModelAttribute User user, BindingResult bindingResult, 
                              @RequestParam("confirmPassword") String confirmPassword, Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        try {
            // Use service layer for user creation with validation
            UserService.ValidationResult result = userService.createUserWithValidation(user, confirmPassword);
            
            if (result.isValid()) {
                model.addAttribute("message", result.getMessage());
                return "login"; // Redirect to login page after registration
            } else {
                model.addAttribute("error", result.getMessage());
                return "register";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }

    // Display login page
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login"; // Thymeleaf template name: login.html
    }
}
