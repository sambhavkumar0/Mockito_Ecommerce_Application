package com.cts.ecommerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.cts.ecommerce.model.Cart;
import com.cts.ecommerce.model.User;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
  Optional<Cart> findByUser(User user);
}
