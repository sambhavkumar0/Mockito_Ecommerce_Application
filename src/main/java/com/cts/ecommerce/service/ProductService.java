package com.cts.ecommerce.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.dto.ProductResponse;
import com.cts.ecommerce.mapper.ProductMapper;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> getAllActiveProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    public List<Product> getAllActiveProductsForView() {
        return productRepository.findByActiveTrue();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));
        return productMapper.toDto(product);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));
    }

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));
        
        productMapper.updateEntity(productRequest, existingProduct);
        Product savedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(savedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public void softDeleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    // Search and filter methods
    public List<Product> searchProductsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveProductsForView();
        }
        return productRepository.findByActiveTrueAndNameContainingIgnoreCase(searchTerm.trim());
    }

    public List<Product> filterProductsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return getAllActiveProductsForView();
        }
        
        if (minPrice == null) {
            return productRepository.findByActiveTrueAndPriceLessThanEqual(maxPrice);
        }
        
        if (maxPrice == null) {
            return productRepository.findByActiveTrueAndPriceGreaterThanEqual(minPrice);
        }
        
        return productRepository.findByActiveTrueAndPriceBetween(minPrice, maxPrice);
    }

    public List<Product> searchAndFilterProducts(String searchTerm, Double minPrice, Double maxPrice) {
        List<Product> products = getAllActiveProductsForView();
        
        // Apply search filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
        }
        
        // Apply price filters
        if (minPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() >= minPrice)
                    .toList();
        }
        
        if (maxPrice != null) {
            products = products.stream()
                    .filter(product -> product.getPrice() <= maxPrice)
                    .toList();
        }
        
        return products;
    }

    public List<Product> getProductsSortedByName(String sortOrder) {
        List<Product> products = getAllActiveProductsForView();
        
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return products.stream()
                    .sorted((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()))
                    .toList();
        } else {
            return products.stream()
                    .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
                    .toList();
        }
    }

    public List<Product> getProductsSortedByPrice(String sortOrder) {
        List<Product> products = getAllActiveProductsForView();
        
        if ("desc".equalsIgnoreCase(sortOrder)) {
            return products.stream()
                    .sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
                    .toList();
        } else {
            return products.stream()
                    .sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                    .toList();
        }
    }
}
