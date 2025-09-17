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
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartPageControllerUnitTest {

    @Mock private CartService cartService;
    @InjectMocks private CartPageController controller;

    private Model model;
    private Principal principal;
    private CartResponse cartResponse;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        principal = () -> "john@example.com";
        
        CartItemResponse item1 = new CartItemResponse();
        item1.setCartItemId(1L);
        item1.setProductId(1L);
        item1.setProductName("Product 1");
        item1.setPrice(100.0);
        item1.setQuantity(2);
        item1.setStock(10);
        
        cartResponse = new CartResponse(1L, Arrays.asList(item1), 200.0);
    }

    @Test
    void viewCart_redirectsWhenNoPrincipal() {
        // When
        String view = controller.viewCart(null, model);

        // Then
        assertThat(view).isEqualTo("redirect:/login");
        verify(cartService, never()).getCart(anyString());
    }

    @Test
    void viewCart_returnsCartViewWhenLoggedIn() {
        // Given
        when(cartService.getCart("john@example.com")).thenReturn(cartResponse);

        // When
        String view = controller.viewCart(principal, model);

        // Then
        assertThat(view).isEqualTo("cart");
        assertThat(model.getAttribute("cart")).isEqualTo(cartResponse);
        verify(cartService).getCart("john@example.com");
    }

    @Test
    void viewCart_withEmptyCart_returnsCartView() {
        // Given
        CartResponse emptyCart = new CartResponse(1L, Collections.emptyList(), 0.0);
        when(cartService.getCart("john@example.com")).thenReturn(emptyCart);

        // When
        String view = controller.viewCart(principal, model);

        // Then
        assertThat(view).isEqualTo("cart");
        assertThat(model.getAttribute("cart")).isEqualTo(emptyCart);
        verify(cartService).getCart("john@example.com");
    }

    @Test
    void viewCart_withMultipleItems_returnsCartView() {
        // Given
        CartItemResponse item2 = new CartItemResponse();
        item2.setCartItemId(2L);
        item2.setProductId(2L);
        item2.setProductName("Product 2");
        item2.setPrice(50.0);
        item2.setQuantity(1);
        item2.setStock(5);
        
        CartResponse multiItemCart = new CartResponse(1L, Arrays.asList(
            cartResponse.getItems().get(0), item2), 250.0);
        when(cartService.getCart("john@example.com")).thenReturn(multiItemCart);

        // When
        String view = controller.viewCart(principal, model);

        // Then
        assertThat(view).isEqualTo("cart");
        assertThat(model.getAttribute("cart")).isEqualTo(multiItemCart);
        assertThat(multiItemCart.getItems()).hasSize(2);
        verify(cartService).getCart("john@example.com");
    }

    @Test
    void viewCart_withServiceException_handlesGracefully() {
        // Given
        when(cartService.getCart("john@example.com"))
            .thenThrow(new RuntimeException("Cart service error"));

        // When & Then
        try {
            controller.viewCart(principal, model);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Cart service error");
        }
        verify(cartService).getCart("john@example.com");
    }
}
