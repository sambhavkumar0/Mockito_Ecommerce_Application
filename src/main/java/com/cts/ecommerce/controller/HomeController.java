package com.cts.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    // HomeController doesn't need any dependencies as landing page doesn't fetch products
    // If you want to show featured products on landing page, inject ProductService
    public HomeController() {
    }

    // Landing page accessible by everyone
    @GetMapping("/")
    public String landingPage(Model model, Principal principal) {
        // If a user is logged in, add their username to the model
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "landing"; // maps to landing.html
    }
}
