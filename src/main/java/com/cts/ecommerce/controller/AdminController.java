package com.cts.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.repository.CartItemRepository;
import org.springframework.transaction.annotation.Transactional;
import com.cts.ecommerce.service.ImageProcessingService;
import com.cts.ecommerce.service.ImageService;
import com.cts.ecommerce.service.OrderService;
import com.cts.ecommerce.service.ProductService;
import com.cts.ecommerce.service.ProductValidationService;
import com.cts.ecommerce.service.UserService;
import com.cts.ecommerce.model.Order;
import com.cts.ecommerce.model.OrderStatus;
import com.cts.ecommerce.model.User;

import java.io.IOException;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ImageProcessingService imageProcessingService;
    private final ImageService imageService;
    private final ProductValidationService productValidationService;
    private final CartItemRepository cartItemRepository;

    @GetMapping
    public String dashboard(Model model) {
        // Get dashboard statistics
        long activeUsersCount = userService.getActiveUsers().size();
        long totalUsersCount = userService.getAllUsers().size();
        long completedOrdersCount = orderService.getOrdersByStatus(OrderStatus.DELIVERED).size();
        double totalRevenue = orderService.getTotalRevenue();
        
        model.addAttribute("activeUsersCount", activeUsersCount);
        model.addAttribute("totalUsersCount", totalUsersCount);
        model.addAttribute("completedOrdersCount", completedOrdersCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("products", productService.getAllProducts());
        
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    @ResponseBody
    public Order getOrderDetails(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderService.updateOrderStatus(id, orderStatus);
            return "redirect:/admin/orders?status=updated";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/orders?error=invalid-status";
        } catch (IllegalStateException e) {
            return "redirect:/admin/orders?error=cannot-change-status&message=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/admin/orders?error=update-failed";
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return "redirect:/admin/orders?status=cancelled";
        } catch (IllegalStateException e) {
            return "redirect:/admin/orders?error=cannot-cancel&message=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/admin/orders?error=cancel-failed";
        }
    }

    // Simple product CRUD pages
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.getAllActiveProductsForView());
        model.addAttribute("product", new Product());
        return "admin/products";
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute Product product, BindingResult bindingResult, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        // Validate image file
        ProductValidationService.ValidationResult imageValidation = productValidationService.validateImageFile(imageFile);
        if (!imageValidation.isValid()) {
            model.addAttribute("error", imageValidation.getErrorMessage());
            model.addAttribute("products", productService.getAllActiveProductsForView());
            return "admin/products";
        }
        
        // Validate product fields
        boolean fieldsValid = productValidationService.validateProductFields(
            product.getName(), 
            product.getDescription(), 
            product.getPrice(), 
            product.getStock(), 
            bindingResult
        );
        
        if (!fieldsValid || bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getAllActiveProductsForView());
            return "admin/products";
        }
        
        try {
            // Handle image upload through service
            String imageUrl = imageService.saveImage(imageFile);
            product.setImageUrl(imageUrl);
            product.setActive(true);
            
            // Convert Product to ProductRequest for service layer
            ProductRequest productRequest = new ProductRequest();
            productRequest.setName(product.getName());
            productRequest.setDescription(product.getDescription());
            productRequest.setPrice(product.getPrice());
            productRequest.setStock(product.getStock());
            productRequest.setImageUrl(product.getImageUrl());
            productRequest.setActive(product.isActive());
            productService.createProduct(productRequest);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            model.addAttribute("products", productService.getAllActiveProductsForView());
            return "admin/products";
        }
        return "redirect:/admin/products?created=true";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute Product product, BindingResult bindingResult, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getAllActiveProductsForView());
            return "admin/products";
        }
        
        try {
            product.setId(id);
            
            // Handle image upload through service
            Product existingProduct = productService.getProductEntityById(id);
            String imageUrl = imageService.handleImageUpdate(imageFile, existingProduct.getImageUrl());
            product.setImageUrl(imageUrl);
            
            // Set default stock if not provided
            if (product.getStock() == null) {
                product.setStock(0);
            }
            
            // Convert Product to ProductRequest for service layer
            ProductRequest productRequest = new ProductRequest();
            productRequest.setName(product.getName());
            productRequest.setDescription(product.getDescription());
            productRequest.setPrice(product.getPrice());
            productRequest.setStock(product.getStock());
            productRequest.setImageUrl(product.getImageUrl());
            productRequest.setActive(product.isActive());
            productService.updateProduct(id, productRequest);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            model.addAttribute("products", productService.getAllActiveProductsForView());
            return "admin/products";
        }
        return "redirect:/admin/products?updated=true";
    }

    @PostMapping("/products/{id}/delete")
    @Transactional
    public String deleteProduct(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductEntityById(id);
            if (product == null) {
                model.addAttribute("error", "Product not found");
                return "redirect:/admin/products?error=not-found";
            }
            
            // Remove from all carts
            cartItemRepository.deleteByProduct(product);
            // Soft delete from listings
            productService.softDeleteProduct(id);
            
            return "redirect:/admin/products?deleted=true";
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            model.addAttribute("error", "Failed to delete product: " + e.getMessage());
            return "redirect:/admin/products?error=delete-failed";
        }
    }

    // Users list (simple read-only or minimal update)
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, java.security.Principal principal) {
        if (principal != null) {
            try {
                User current = userService.findByEmail(principal.getName());
                if (current.getId().equals(id)) {
                    return "redirect:/admin/users?error=self-delete";
                }
            } catch (Exception e) {
                // User not found, continue with deletion
            }
        }
        // Prevent deleting users who still have orders; ask admin to transfer/cleanup first
        try {
            if (orderService.userHasOrders(id)) {
                return "redirect:/admin/users?error=has-orders";
            }
            userService.deleteUser(id);
            return "redirect:/admin/users?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/users?error=delete-failed";
        }
    }

    // User status management endpoints
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, java.security.Principal principal) {
        try {
            // Prevent admin from deactivating themselves
            if (principal != null) {
                User current = userService.findByEmail(principal.getName());
                if (current.getId().equals(id)) {
                    return "redirect:/admin/users?error=self-deactivate";
                }
            }
            
            userService.toggleUserStatus(id);
            return "redirect:/admin/users?status=toggled";
        } catch (Exception e) {
            return "redirect:/admin/users?error=status-failed";
        }
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id) {
        try {
            userService.activateUser(id);
            return "redirect:/admin/users?status=activated";
        } catch (Exception e) {
            return "redirect:/admin/users?error=activate-failed";
        }
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, java.security.Principal principal) {
        try {
            // Prevent admin from deactivating themselves
            if (principal != null) {
                User current = userService.findByEmail(principal.getName());
                if (current.getId().equals(id)) {
                    return "redirect:/admin/users?error=self-deactivate";
                }
            }
            
            userService.deactivateUser(id);
            return "redirect:/admin/users?status=deactivated";
        } catch (Exception e) {
            return "redirect:/admin/users?error=deactivate-failed";
        }
    }
}


