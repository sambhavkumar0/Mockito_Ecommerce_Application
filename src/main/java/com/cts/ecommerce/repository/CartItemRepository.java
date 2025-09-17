package com.cts.ecommerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.cts.ecommerce.model.CartItem;
import com.cts.ecommerce.model.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByProduct(Product product);
    void deleteByProduct(Product product);
}
