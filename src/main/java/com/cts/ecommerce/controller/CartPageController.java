package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cts.ecommerce.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartPageController {

    private final CartService cartService;

    // ✅ Cart page (Thymeleaf)
    @GetMapping("/cart")
    public String viewCart(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        // Principal.getName() now returns the email, but cartService expects username
        // We need to find the user by email first to get the username
        String email = principal.getName();
        // For now, we'll use the email as the identifier in cart service
        // You might need to update CartService to work with email instead of username
        model.addAttribute("cart", cartService.getCart(email));
        return "cart"; // maps to cart.html
    }
    
    
}
