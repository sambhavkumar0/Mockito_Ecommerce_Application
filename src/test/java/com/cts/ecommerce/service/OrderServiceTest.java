package com.cts.ecommerce.service;

import com.cts.ecommerce.model.*;
import com.cts.ecommerce.repository.CartRepository;
import com.cts.ecommerce.repository.OrderRepository;
import com.cts.ecommerce.repository.ProductRepository;
import com.cts.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;

	@InjectMocks
	private OrderService orderService;

	private User user;
	private Cart cart;
    private Product product1;
    private Product product2;
    private Order order;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("john@example.com");

		cart = new Cart();
		cart.setId(10L);
		cart.setUser(user);
		cart.setItems(new ArrayList<>());

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Product A");
        product1.setPrice(100.0);
        product1.setStock(10);

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Product B");
        product2.setPrice(50.0);
        product2.setStock(5);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalPrice(250.0);
        order.setItems(new ArrayList<>());
	}

	@Test
	void placeOrder_transfersCartItemsAndClearsCart() {
        // Given
        CartItem ci1 = new CartItem();
        ci1.setCart(cart);
        ci1.setProduct(product1);
        ci1.setQuantity(2);

        CartItem ci2 = new CartItem();
        ci2.setCart(cart);
        ci2.setProduct(product2);
        ci2.setQuantity(1);

		cart.getItems().add(ci1);
		cart.getItems().add(ci2);

		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
		when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
		when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
		Order saved = orderService.placeOrder(user.getEmail());

        // Then
		assertThat(saved.getUser()).isEqualTo(user);
		assertThat(saved.getItems()).hasSize(2);
		assertThat(saved.getTotalPrice()).isEqualTo(250.0);
		assertThat(cart.getItems()).isEmpty();
		verify(cartRepository).save(cart);
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void placeOrder_insufficientStock_throwsException() {
        // Given
        product1.setStock(1); // Only 1 in stock
        CartItem ci1 = new CartItem();
        ci1.setCart(cart);
        ci1.setProduct(product1);
        ci1.setQuantity(2); // But trying to order 2

        cart.getItems().add(ci1);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.placeOrder(user.getEmail()));
        
        assertThat(exception.getMessage()).contains("Insufficient stock");
        assertThat(exception.getMessage()).contains("Available: 1");
        assertThat(exception.getMessage()).contains("Requested: 2");
	}

	@Test
	void getOrders_returnsOrdersForUser() {
        // Given
		when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        List<Order> orders = Arrays.asList(order);
		when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(orders);

        // When
		List<Order> result = orderService.getOrders(user.getEmail());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(order);
    }

    @Test
    void getAllOrders_returnsAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(order);
    }

    @Test
    void getOrderById_returnsOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        Order result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isEqualTo(order);
    }

    @Test
    void getOrderById_notFound_throwsException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
    }

    @Test
    void userHasOrders_returnsTrue() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUser(user)).thenReturn(true);

        // When
        boolean result = orderService.userHasOrders(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userHasOrders_returnsFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsByUser(user)).thenReturn(false);

        // When
        boolean result = orderService.userHasOrders(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateOrderOwnership_returnsTrue() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList(order));

        // When
        boolean result = orderService.validateOrderOwnership(1L, user.getEmail());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateOrderOwnership_returnsFalse() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList());

        // When
        boolean result = orderService.validateOrderOwnership(1L, user.getEmail());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void cancelOrderWithValidation_success() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList(order));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.cancelOrderWithValidation(1L, user.getEmail());

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrderWithValidation_notOwned_throwsException() {
        // Given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(Arrays.asList());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> orderService.cancelOrderWithValidation(1L, user.getEmail()));
        
        assertThat(exception.getMessage()).contains("Order not found or does not belong to current user");
    }

    @Test
    void updateOrderStatus_success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.updateOrderStatus(1L, OrderStatus.DELIVERED);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_notPlaced_throwsException() {
        // Given
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.updateOrderStatus(1L, OrderStatus.DELIVERED));
        
        assertThat(exception.getMessage()).contains("Cannot change status of delivered orders");
    }

    @Test
    void updateOrderStatus_toCancelled_restoresStock() {
        // Given
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product1);
        orderItem.setQuantity(2);
        order.getItems().add(orderItem);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.updateOrderStatus(1L, OrderStatus.CANCELLED);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product1.getStock()).isEqualTo(12); // 10 + 2
        verify(productRepository).save(product1);
    }

    @Test
    void cancelOrder_success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Order result = orderService.cancelOrder(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_notPlaced_throwsException() {
        // Given
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder(1L));
        
        assertThat(exception.getMessage()).contains("Cannot cancel delivered orders");
    }

    @Test
    void getOrdersByStatus_returnsOrders() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.PLACED)).thenReturn(orders);

        // When
        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PLACED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(order);
    }

    @Test
    void getTotalRevenue_returnsSum() {
        // Given
        Order deliveredOrder1 = new Order();
        deliveredOrder1.setTotalPrice(100.0);
        
        Order deliveredOrder2 = new Order();
        deliveredOrder2.setTotalPrice(150.0);
        
        List<Order> deliveredOrders = Arrays.asList(deliveredOrder1, deliveredOrder2);
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.DELIVERED)).thenReturn(deliveredOrders);

        // When
        double result = orderService.getTotalRevenue();

        // Then
        assertThat(result).isEqualTo(250.0);
	}
}
