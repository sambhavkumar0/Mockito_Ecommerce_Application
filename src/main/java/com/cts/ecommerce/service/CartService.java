package com.cts.ecommerce.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cts.ecommerce.dto.CartResponse;
import com.cts.ecommerce.mapper.CartMapper;
import com.cts.ecommerce.model.Cart;
import com.cts.ecommerce.model.CartItem;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.model.User;
import com.cts.ecommerce.repository.CartItemRepository;
import com.cts.ecommerce.repository.CartRepository;
import com.cts.ecommerce.repository.ProductRepository;
import com.cts.ecommerce.repository.UserRepository;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    // Get current user's cart
    public CartResponse getCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
        return cartMapper.toDto(cart);
    }

    // Add product to cart (or increase quantity)
    public CartResponse addToCart(String email, Long productId, int quantity) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }

        return cartMapper.toDto(cartRepository.save(cart));
    }

    // Remove product from cart
    public CartResponse removeFromCart(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        return cartMapper.toDto(cartRepository.save(cart));
    }

    // Clear cart
    public CartResponse clearCart(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();

        cart.getItems().clear();

        return cartMapper.toDto(cartRepository.save(cart));
    }

    // Increase quantity of a cart item
    public CartResponse increaseQuantity(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);

        return cartMapper.toDto(item.getCart());
    }

    // Decrease quantity (auto-remove if 0)
    public CartResponse decreaseQuantity(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        Cart cart = item.getCart();
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartItemRepository.save(item);
        } else {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        }

        return cartMapper.toDto(cartRepository.save(cart));
    }
}
