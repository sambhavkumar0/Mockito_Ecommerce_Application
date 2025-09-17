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
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersPageControllerUnitTest {

    @Mock private OrderService orderService;
    @InjectMocks private OrdersPageController controller;

    private Model model;
    private Principal principal;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        principal = () -> "john@example.com";
        
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PLACED);
        order1.setTotalPrice(100.0);
        
        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setTotalPrice(150.0);
        
        orders = Arrays.asList(order1, order2);
    }

    @Test
    void orders_redirectsWhenNoPrincipal() {
        // When
        String view = controller.orders(model, null);

        // Then
        assertThat(view).isEqualTo("redirect:/login");
        verify(orderService, never()).getOrders(anyString());
    }

    @Test
    void orders_returnsOrdersViewWhenLoggedIn() {
        // Given
        when(orderService.getOrders("john@example.com")).thenReturn(orders);

        // When
        String view = controller.orders(model, principal);

        // Then
        assertThat(view).isEqualTo("orders");
        assertThat(model.getAttribute("orders")).isEqualTo(orders);
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void orders_withEmptyOrdersList_returnsOrdersView() {
        // Given
        when(orderService.getOrders("john@example.com")).thenReturn(Collections.emptyList());

        // When
        String view = controller.orders(model, principal);

        // Then
        assertThat(view).isEqualTo("orders");
        assertThat(model.getAttribute("orders")).isEqualTo(Collections.emptyList());
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void orders_withSingleOrder_returnsOrdersView() {
        // Given
        List<Order> singleOrder = Arrays.asList(orders.get(0));
        when(orderService.getOrders("john@example.com")).thenReturn(singleOrder);

        // When
        String view = controller.orders(model, principal);

        // Then
        assertThat(view).isEqualTo("orders");
        assertThat(model.getAttribute("orders")).isEqualTo(singleOrder);
        assertThat(singleOrder).hasSize(1);
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void orders_withServiceException_handlesGracefully() {
        // Given
        when(orderService.getOrders("john@example.com"))
            .thenThrow(new RuntimeException("Order service error"));

        // When & Then
        try {
            controller.orders(model, principal);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Order service error");
        }
        verify(orderService).getOrders("john@example.com");
    }

    @Test
    void orders_withNullPrincipal_redirectsToLogin() {
        // When
        String view = controller.orders(model, null);

        // Then
        assertThat(view).isEqualTo("redirect:/login");
        verify(orderService, never()).getOrders(anyString());
    }
}
