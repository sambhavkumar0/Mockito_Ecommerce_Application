package com.cts.ecommerce.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.dto.ProductResponse;
import com.cts.ecommerce.mapper.ProductMapper;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    // ==================== REST API ====================

    // ✅ Get all products (API)
    @GetMapping("/api")
    @ResponseBody
    public List<ProductResponse> listAllApi() {
        return productService.getAllActiveProducts();
    }

    // ✅ Search products (API)
    @GetMapping("/api/search")
    @ResponseBody
    public List<ProductResponse> searchProductsApi(@RequestParam(required = false) String search,
                                                  @RequestParam(required = false) Double minPrice,
                                                  @RequestParam(required = false) Double maxPrice,
                                                  @RequestParam(required = false) String sortBy,
                                                  @RequestParam(required = false) String sortOrder) {
        
        List<Product> products;
        
        // Apply search and filters
        if (search != null || minPrice != null || maxPrice != null) {
            products = productService.searchAndFilterProducts(search, minPrice, maxPrice);
        } else {
            products = productService.getAllActiveProductsForView();
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("name".equals(sortBy)) {
                products = productService.getProductsSortedByName(sortOrder);
            } else if ("price".equals(sortBy)) {
                products = productService.getProductsSortedByPrice(sortOrder);
            }
        }
        
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    // ✅ Get product by ID (API)
    @GetMapping("/api/{id}")
    @ResponseBody
    public ProductResponse getByIdApi(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // ✅ Create new product ( ADMIN ) (API)
    
    @PostMapping("/api/admin")
    @ResponseBody
    public ProductResponse createApi(@Valid @RequestBody ProductRequest dto) {
        return productService.createProduct(dto);
    }

    // ✅ Update product (Admin) (API)
    @PutMapping("/api/admin/{id}")
    @ResponseBody
    public ProductResponse updateApi(@PathVariable Long id, @Valid @RequestBody ProductRequest dto) {
        return productService.updateProduct(id, dto);
    }

    // ✅ Delete product (Admin) (API)
    @DeleteMapping("/api/admin/{id}")
    @ResponseBody
    public void deleteApi(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    // ==================== Thymeleaf Pages ====================

    // Products / Shop page
    @GetMapping
    public String productsPage(@RequestParam(required = false) String search,
                              @RequestParam(required = false) Double minPrice,
                              @RequestParam(required = false) Double maxPrice,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String sortOrder,
                              Model model) {
        
        List<Product> products;
        
        // Apply search and filters
        if (search != null || minPrice != null || maxPrice != null) {
            products = productService.searchAndFilterProducts(search, minPrice, maxPrice);
        } else {
            products = productService.getAllActiveProductsForView();
        }
        
        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            if ("name".equals(sortBy)) {
                products = productService.getProductsSortedByName(sortOrder);
            } else if ("price".equals(sortBy)) {
                products = productService.getProductsSortedByPrice(sortOrder);
            }
        }
        
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);
        
        return "products"; // maps to products.html Thymeleaf template
    }

    // Product detail page
    @GetMapping("/{id}")
    public String productDetailPage(@PathVariable Long id, Model model) {
        Product product = productService.getProductEntityById(id);
        model.addAttribute("product", product);
        return "product-detail"; // maps to product-detail.html Thymeleaf template
    }
}
