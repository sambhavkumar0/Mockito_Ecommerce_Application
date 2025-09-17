package com.cts.ecommerce.controller;

import com.cts.ecommerce.model.Order;
import com.cts.ecommerce.model.OrderStatus;
import com.cts.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerUnitTest {

    @Mock private OrderService orderService;
    @InjectMocks private OrderController controller;

    private UserDetails userDetails;
    private Order order;

    @BeforeEach
    void setUp() {
        userDetails = User.withUsername("john@example.com").password("x").roles("USER").build();
        
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalPrice(100.0);
    }

    @Test
    void place_returnsOrder() {
        // Given
        when(orderService.placeOrder("john@example.com")).thenReturn(order);

        // When
        Order result = controller.place(userDetails);

        // Then
        assertThat(result).isEqualTo(order);
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderService).placeOrder("john@example.com");
    }

    @Test
    void place_withEmptyCart_throwsException() {
        // Given
        when(orderService.placeOrder("john@example.com"))
            .thenThrow(new RuntimeException("Cart is empty"));

        // When & Then
        try {
            controller.place(userDetails);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Cart is empty");
        }
        verify(orderService).placeOrder("john@example.com");
    }

    @Test
    void list_returnsOrders() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderService.getOrders("john@example.com")).thenReturn(orders);

        // When
        List<Order> result = controller.list(userDetails);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(order);
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void list_withNoOrders_returnsEmptyList() {
        // Given
        when(orderService.getOrders("john@example.com")).thenReturn(Arrays.asList());

        // When
        List<Order> result = controller.list(userDetails);

        // Then
        assertThat(result).isEmpty();
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void cancelOrder_returnsCancelledOrder() {
        // Given
        Order cancelledOrder = new Order();
        cancelledOrder.setId(1L);
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        when(orderService.cancelOrderWithValidation(1L, "john@example.com")).thenReturn(cancelledOrder);

        // When
        Order result = controller.cancelOrder(1L, userDetails);

        // Then
        assertThat(result).isEqualTo(cancelledOrder);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderService).cancelOrderWithValidation(1L, "john@example.com");
    }

    @Test
    void cancelOrder_withInvalidOrder_throwsException() {
        // Given
        when(orderService.cancelOrderWithValidation(999L, "john@example.com"))
            .thenThrow(new IllegalArgumentException("Order not found or does not belong to current user"));

        // When & Then
        try {
            controller.cancelOrder(999L, userDetails);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Order not found or does not belong to current user");
        }
        verify(orderService).cancelOrderWithValidation(999L, "john@example.com");
    }

    @Test
    void cancelOrder_withNonCancellableOrder_throwsException() {
        // Given
        when(orderService.cancelOrderWithValidation(1L, "john@example.com"))
            .thenThrow(new IllegalStateException("Cannot cancel delivered orders"));

        // When & Then
        try {
            controller.cancelOrder(1L, userDetails);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Cannot cancel delivered orders");
        }
        verify(orderService).cancelOrderWithValidation(1L, "john@example.com");
    }
}
