package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.cts.ecommerce.dto.CartResponse;
import com.cts.ecommerce.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Get cart
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails ud) {
        return cartService.getCart(ud.getUsername()); // This is now the email
    }
   

    // Add product to cart
    @PostMapping("/add/{productId}")
    public CartResponse addToCart(@AuthenticationPrincipal UserDetails ud,
                                  @PathVariable Long productId,
                                  @RequestParam(defaultValue = "1") int quantity) {
        return cartService.addToCart(ud.getUsername(), productId, quantity); // This is now the email
    }

    // Remove product from cart
    @DeleteMapping("/remove/{productId}")
    public CartResponse removeFromCart(@AuthenticationPrincipal UserDetails ud,
                                       @PathVariable Long productId) {
        return cartService.removeFromCart(ud.getUsername(), productId); // This is now the email
    }

    // Clear cart
    @DeleteMapping("/clear")
    public CartResponse clearCart(@AuthenticationPrincipal UserDetails ud) {
        return cartService.clearCart(ud.getUsername()); // This is now the email
    }

    // Increase quantity
    @PostMapping("/increase/{cartItemId}")
    public CartResponse increaseQuantity(@PathVariable Long cartItemId) {
        return cartService.increaseQuantity(cartItemId);
    }

    // Decrease quantity
    @PostMapping("/decrease/{cartItemId}")
    public CartResponse decreaseQuantity(@PathVariable Long cartItemId) {
        return cartService.decreaseQuantity(cartItemId);
    }

}
