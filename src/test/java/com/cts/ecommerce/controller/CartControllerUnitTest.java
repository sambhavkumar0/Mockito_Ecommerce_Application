package com.cts.ecommerce.controller;

import com.cts.ecommerce.dto.CartItemResponse;
import com.cts.ecommerce.dto.CartResponse;
import com.cts.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerUnitTest {

    @Mock private CartService cartService;
    @InjectMocks private CartController controller;

    private UserDetails user;
    private CartResponse cartResponse;
    private List<CartItemResponse> cartItems;

    @BeforeEach
    void setUp() {
        user = User.withUsername("john@example.com").password("x").roles("USER").build();
        
        CartItemResponse item1 = new CartItemResponse();
        item1.setCartItemId(1L);
        item1.setProductId(1L);
        item1.setProductName("Product 1");
        item1.setPrice(100.0);
        item1.setQuantity(2);
        item1.setStock(10);
        
        CartItemResponse item2 = new CartItemResponse();
        item2.setCartItemId(2L);
        item2.setProductId(2L);
        item2.setProductName("Product 2");
        item2.setPrice(50.0);
        item2.setQuantity(1);
        item2.setStock(5);
        cartItems = Arrays.asList(item1, item2);
        
        cartResponse = new CartResponse(1L, cartItems, 250.0);
    }

    @Test
    void getCart_returnsCartResponse() {
        // Given
        when(cartService.getCart("john@example.com")).thenReturn(cartResponse);

        // When
        CartResponse result = controller.getCart(user);

        // Then
        assertThat(result).isEqualTo(cartResponse);
        assertThat(result.getCartId()).isEqualTo(1L);
        assertThat(result.getTotalPrice()).isEqualTo(250.0);
        assertThat(result.getItems()).hasSize(2);
        verify(cartService).getCart("john@example.com");
    }

    @Test
    void getCart_withEmptyCart() {
        // Given
        CartResponse emptyCart = new CartResponse(1L, Collections.emptyList(), 0.0);
        when(cartService.getCart("john@example.com")).thenReturn(emptyCart);

        // When
        CartResponse result = controller.getCart(user);

        // Then
        assertThat(result).isEqualTo(emptyCart);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalPrice()).isEqualTo(0.0);
        verify(cartService).getCart("john@example.com");
    }

    @Test
    void addToCart_returnsUpdatedCartResponse() {
        // Given
        when(cartService.addToCart("john@example.com", 1L, 2)).thenReturn(cartResponse);

        // When
        CartResponse result = controller.addToCart(user, 1L, 2);

        // Then
        assertThat(result).isEqualTo(cartResponse);
        verify(cartService).addToCart("john@example.com", 1L, 2);
    }

    @Test
    void addToCart_withDefaultQuantity() {
        // Given
        when(cartService.addToCart("john@example.com", 1L, 1)).thenReturn(cartResponse);

        // When
        CartResponse result = controller.addToCart(user, 1L, 1);

        // Then
        assertThat(result).isEqualTo(cartResponse);
        verify(cartService).addToCart("john@example.com", 1L, 1);
    }

    @Test
    void removeFromCart_returnsUpdatedCartResponse() {
        // Given
        CartResponse updatedCart = new CartResponse(1L, Arrays.asList(cartItems.get(1)), 50.0);
        when(cartService.removeFromCart("john@example.com", 1L)).thenReturn(updatedCart);

        // When
        CartResponse result = controller.removeFromCart(user, 1L);

        // Then
        assertThat(result).isEqualTo(updatedCart);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalPrice()).isEqualTo(50.0);
        verify(cartService).removeFromCart("john@example.com", 1L);
    }

    @Test
    void clearCart_returnsEmptyCartResponse() {
        // Given
        CartResponse emptyCart = new CartResponse(1L, Collections.emptyList(), 0.0);
        when(cartService.clearCart("john@example.com")).thenReturn(emptyCart);

        // When
        CartResponse result = controller.clearCart(user);

        // Then
        assertThat(result).isEqualTo(emptyCart);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalPrice()).isEqualTo(0.0);
        verify(cartService).clearCart("john@example.com");
    }

    @Test
    void increaseQuantity_returnsUpdatedCartResponse() {
        // Given
        CartResponse updatedCart = new CartResponse(1L, cartItems, 300.0);
        when(cartService.increaseQuantity(1L)).thenReturn(updatedCart);

        // When
        CartResponse result = controller.increaseQuantity(1L);

        // Then
        assertThat(result).isEqualTo(updatedCart);
        assertThat(result.getTotalPrice()).isEqualTo(300.0);
        verify(cartService).increaseQuantity(1L);
    }

    @Test
    void decreaseQuantity_returnsUpdatedCartResponse() {
        // Given
        CartResponse updatedCart = new CartResponse(1L, cartItems, 200.0);
        when(cartService.decreaseQuantity(1L)).thenReturn(updatedCart);

        // When
        CartResponse result = controller.decreaseQuantity(1L);

        // Then
        assertThat(result).isEqualTo(updatedCart);
        assertThat(result.getTotalPrice()).isEqualTo(200.0);
        verify(cartService).decreaseQuantity(1L);
    }

    @Test
    void decreaseQuantity_removesItemWhenZero() {
        // Given
        CartResponse updatedCart = new CartResponse(1L, Arrays.asList(cartItems.get(1)), 50.0);
        when(cartService.decreaseQuantity(1L)).thenReturn(updatedCart);

        // When
        CartResponse result = controller.decreaseQuantity(1L);

        // Then
        assertThat(result).isEqualTo(updatedCart);
        assertThat(result.getItems()).hasSize(1);
        verify(cartService).decreaseQuantity(1L);
    }

    @Test
    void allMethods_useCorrectEmailFromUserDetails() {
        // Given
        when(cartService.getCart("john@example.com")).thenReturn(cartResponse);
        when(cartService.addToCart("john@example.com", 1L, 1)).thenReturn(cartResponse);
        when(cartService.removeFromCart("john@example.com", 1L)).thenReturn(cartResponse);
        when(cartService.clearCart("john@example.com")).thenReturn(cartResponse);

        // When
        controller.getCart(user);
        controller.addToCart(user, 1L, 1);
        controller.removeFromCart(user, 1L);
        controller.clearCart(user);

        // Then
        verify(cartService).getCart("john@example.com");
        verify(cartService).addToCart("john@example.com", 1L, 1);
        verify(cartService).removeFromCart("john@example.com", 1L);
        verify(cartService).clearCart("john@example.com");
    }
}
