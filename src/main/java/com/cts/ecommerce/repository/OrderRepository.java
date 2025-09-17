package com.cts.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.ecommerce.model.Order;
import com.cts.ecommerce.model.OrderStatus;
import com.cts.ecommerce.model.User;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    boolean existsByUser(User user);
}


