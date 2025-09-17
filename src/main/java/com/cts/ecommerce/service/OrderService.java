package com.cts.ecommerce.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.ecommerce.model.*;
import com.cts.ecommerce.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order placeOrder(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        Cart cart = cartRepository.findByUser(user).orElseThrow(EntityNotFoundException::new);

        Order order = new Order();
        order.setUser(user);

        double total = 0.0;
        for (CartItem ci : cart.getItems()) {
            // Check stock availability before placing order
            Product product = ci.getProduct();
            if (product.getStock() < ci.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getStock() + ", Requested: " + ci.getQuantity());
            }
            
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setProductName(product.getName());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(product.getPrice());
            order.getItems().add(oi);
            total += oi.getPrice() * oi.getQuantity();
        }
        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);

        // Reduce stock for all products in the order
        reduceStockForOrder(saved);

        cart.getItems().clear();
        cartRepository.save(cart);

        return saved;
    }

    public List<Order> getOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        // Initialize lazy relations needed for JSON
        if (order.getItems() != null) {
            order.getItems().size();
        }
        if (order.getUser() != null) {
            order.getUser().getId();
        }
        return order;
    }

    public boolean userHasOrders(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        return orderRepository.existsByUser(user);
    }

    /**
     * Validates that an order belongs to a specific user
     * @param orderId Order ID to validate
     * @param userEmail User email to check ownership
     * @return true if order belongs to user, false otherwise
     */
    public boolean validateOrderOwnership(Long orderId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(EntityNotFoundException::new);
        List<Order> userOrders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return userOrders.stream().anyMatch(order -> order.getId().equals(orderId));
    }

    /**
     * Cancels an order with ownership validation
     * @param orderId Order ID to cancel
     * @param userEmail User email for ownership validation
     * @return Cancelled order
     * @throws IllegalArgumentException if order doesn't belong to user
     */
    @Transactional
    public Order cancelOrderWithValidation(Long orderId, String userEmail) {
        if (!validateOrderOwnership(orderId, userEmail)) {
            throw new IllegalArgumentException("Order not found or does not belong to current user");
        }
        return cancelOrder(orderId);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Only allow status changes for PLACED orders
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Cannot change status of " + order.getStatus().getDisplayName().toLowerCase() + " orders. Only placed orders can be modified.");
        }
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // If order is being cancelled, restore stock
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Only allow cancellation of PLACED orders
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Cannot cancel " + order.getStatus().getDisplayName().toLowerCase() + " orders. Only placed orders can be cancelled.");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        restoreStockForOrder(order);
        
        return orderRepository.save(order);
    }

    private void reduceStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private void restoreStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public double getTotalRevenue() {
        List<Order> completedOrders = orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.DELIVERED);
        return completedOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }
}


