package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.validation.Valid;

import com.cts.ecommerce.model.User;
import com.cts.ecommerce.service.UserService;
import com.cts.ecommerce.dto.ProfileUpdateRequest;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // This is now the email
        
        User user = userService.getUserProfile(email);
        
        // Create ProfileUpdateRequest with current user data
        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setEmail(user.getEmail());
        profileUpdateRequest.setFirstName(user.getFirstName());
        profileUpdateRequest.setLastName(user.getLastName());
        
        model.addAttribute("user", user);
        model.addAttribute("profileUpdateRequest", profileUpdateRequest);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute ProfileUpdateRequest request, 
                               BindingResult bindingResult, 
                               Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            // Re-populate user data for the form
            User user = userService.getUserProfile(currentEmail);
            model.addAttribute("user", user);
            model.addAttribute("profileUpdateRequest", request);
            return "profile";
        }
        
        // Use service to update profile
        UserService.ValidationResult result = userService.updateUserProfile(currentEmail, request);
        
        if (result.isValid()) {
            return "redirect:/profile?updated=true";
        } else {
            // Re-populate user data for the form
            User user = userService.getUserProfile(currentEmail);
            model.addAttribute("error", result.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("profileUpdateRequest", request);
            return "profile";
        }
    }
}
