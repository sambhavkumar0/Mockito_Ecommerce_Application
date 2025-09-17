package com.cts.ecommerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import com.cts.ecommerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue(); //SELECT * FROM product WHERE active = true;

    
    // Search methods
    //SELECT * FROM product WHERE active = true AND LOWER(name) LIKE LOWER('%:name%');

    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);
    
    // Price filter methods
    List<Product> findByActiveTrueAndPriceGreaterThanEqual(Double minPrice);
    List<Product> findByActiveTrueAndPriceLessThanEqual(Double maxPrice);
    List<Product> findByActiveTrueAndPriceBetween(Double minPrice, Double maxPrice);
}
