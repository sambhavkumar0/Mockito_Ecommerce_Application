package com.cts.ecommerce.controller;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.model.*;
import com.cts.ecommerce.repository.CartItemRepository;
import com.cts.ecommerce.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock private UserService userService;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;
    @Mock private ImageProcessingService imageProcessingService;
    @Mock private ImageService imageService;
    @Mock private ProductValidationService productValidationService;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private Principal principal;

    @InjectMocks private AdminController controller;

    private Model model;
    private User user;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        
        user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        user.setActive(true);
        
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(100.0);
        product.setStock(10);
        product.setActive(true);
        
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalPrice(100.0);
    }

    @Test
    void dashboard_addsAttributesAndReturnsView() {
        // Given
        List<User> users = Arrays.asList(user);
        List<Product> products = Arrays.asList(product);
        List<Order> orders = Arrays.asList(order);
        
        when(userService.getActiveUsers()).thenReturn(users);
        when(userService.getAllUsers()).thenReturn(users);
        when(orderService.getOrdersByStatus(OrderStatus.DELIVERED)).thenReturn(orders);
        when(orderService.getTotalRevenue()).thenReturn(100.0);
        when(productService.getAllProducts()).thenReturn(products);

        // When
        String view = controller.dashboard(model);

        // Then
        assertThat(view).isEqualTo("admin/dashboard");
        assertThat(model.getAttribute("activeUsersCount")).isEqualTo(1L);
        assertThat(model.getAttribute("totalUsersCount")).isEqualTo(1L);
        assertThat(model.getAttribute("completedOrdersCount")).isEqualTo(1L);
        assertThat(model.getAttribute("totalRevenue")).isEqualTo(100.0);
        assertThat(model.getAttribute("users")).isEqualTo(users);
        assertThat(model.getAttribute("products")).isEqualTo(products);
    }

    @Test
    void orders_returnsView() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderService.getAllOrders()).thenReturn(orders);

        // When
        String view = controller.orders(model);

        // Then
        assertThat(view).isEqualTo("admin/orders");
        assertThat(model.getAttribute("orders")).isEqualTo(orders);
    }

    @Test
    void getOrderDetails_returnsOrder() {
        // Given
        when(orderService.getOrderById(1L)).thenReturn(order);

        // When
        Order result = controller.getOrderDetails(1L);

        // Then
        assertThat(result).isEqualTo(order);
        verify(orderService).getOrderById(1L);
    }

    @Test
    void updateOrderStatus_success() {
        // Given
        when(orderService.updateOrderStatus(1L, OrderStatus.DELIVERED)).thenReturn(order);

        // When
        String result = controller.updateOrderStatus(1L, "shipped");

        // Then
        assertThat(result).isEqualTo("redirect:/admin/orders?status=updated");
        verify(orderService).updateOrderStatus(1L, OrderStatus.DELIVERED);
    }

    @Test
    void updateOrderStatus_invalidStatus() {
        // When
        String result = controller.updateOrderStatus(1L, "invalid");

        // Then
        assertThat(result).isEqualTo("redirect:/admin/orders?error=invalid-status");
    }

    @Test
    void updateOrderStatus_cannotChangeStatus() {
        // Given
        when(orderService.updateOrderStatus(1L, OrderStatus.DELIVERED))
            .thenThrow(new IllegalStateException("Cannot change status"));

        // When
        String result = controller.updateOrderStatus(1L, "shipped");

        // Then
        assertThat(result).isEqualTo("redirect:/admin/orders?error=cannot-change-status&message=Cannot change status");
    }

    @Test
    void cancelOrder_success() {
        // Given
        when(orderService.cancelOrder(1L)).thenReturn(order);

        // When
        String result = controller.cancelOrder(1L);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/orders?status=cancelled");
        verify(orderService).cancelOrder(1L);
    }

    @Test
    void cancelOrder_cannotCancel() {
        // Given
        when(orderService.cancelOrder(1L))
            .thenThrow(new IllegalStateException("Cannot cancel order"));

        // When
        String result = controller.cancelOrder(1L);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/orders?error=cannot-cancel&message=Cannot cancel order");
    }

    @Test
    void products_returnsView() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getAllActiveProductsForView()).thenReturn(products);

        // When
        String view = controller.products(model);

        // Then
        assertThat(view).isEqualTo("admin/products");
        assertThat(model.getAttribute("products")).isEqualTo(products);
        assertThat(model.getAttribute("product")).isInstanceOf(Product.class);
    }

    @Test
    void createProduct_success() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        ProductValidationService.ValidationResult validationResult = 
            new ProductValidationService.ValidationResult(true, null);
        when(productValidationService.validateImageFile(file)).thenReturn(validationResult);
        when(productValidationService.validateProductFields(any(), any(), any(), any(), any()))
            .thenReturn(true);
        when(imageService.saveImage(file)).thenReturn("image-url");
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.createProduct(product, bindingResult, file, model);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/products?created=true");
        verify(productService).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProduct_imageValidationFails() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        ProductValidationService.ValidationResult validationResult = 
            new ProductValidationService.ValidationResult(false, "Invalid image");
        when(productValidationService.validateImageFile(file)).thenReturn(validationResult);
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.createProduct(product, bindingResult, file, model);

        // Then
        assertThat(result).isEqualTo("admin/products");
        assertThat(model.getAttribute("error")).isEqualTo("Invalid image");
    }

    @Test
    void createProduct_fieldValidationFails() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        
        ProductValidationService.ValidationResult validationResult = 
            new ProductValidationService.ValidationResult(true, null);
        when(productValidationService.validateImageFile(file)).thenReturn(validationResult);
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.createProduct(product, bindingResult, file, model);

        // Then
        assertThat(result).isEqualTo("admin/products");
    }

    @Test
    void updateProduct_success() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(productService.getProductEntityById(1L)).thenReturn(product);
        when(imageService.handleImageUpdate(file, product.getImageUrl())).thenReturn("new-image-url");
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.updateProduct(1L, product, bindingResult, file, model);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/products?updated=true");
        verify(productService).updateProduct(eq(1L), any(ProductRequest.class));
    }

    @Test
    void deleteProduct_success() {
        // Given
        when(productService.getProductEntityById(1L)).thenReturn(product);
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.deleteProduct(1L, model);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/products?deleted=true");
        verify(cartItemRepository).deleteByProduct(product);
        verify(productService).softDeleteProduct(1L);
    }

    @Test
    void deleteProduct_notFound() {
        // Given
        when(productService.getProductEntityById(1L)).thenReturn(null);
        when(productService.getAllActiveProductsForView()).thenReturn(Arrays.asList());

        // When
        String result = controller.deleteProduct(1L, model);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/products?error=not-found");
    }

    @Test
    void users_returnsView() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userService.getAllUsers()).thenReturn(users);

        // When
        String view = controller.users(model);

        // Then
        assertThat(view).isEqualTo("admin/users");
        assertThat(model.getAttribute("users")).isEqualTo(users);
    }

    @Test
    void deleteUser_success() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);
        when(orderService.userHasOrders(1L)).thenReturn(false);

        // When
        String result = controller.deleteUser(1L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?deleted=true");
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_selfDelete() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        // When
        String result = controller.deleteUser(1L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?error=self-delete");
    }

    @Test
    void deleteUser_hasOrders() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);
        when(orderService.userHasOrders(1L)).thenReturn(true);

        // When
        String result = controller.deleteUser(1L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?error=has-orders");
    }

    @Test
    void toggleUserStatus_success() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        // When
        String result = controller.toggleUserStatus(2L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?status=toggled");
        verify(userService).toggleUserStatus(2L);
    }

    @Test
    void toggleUserStatus_selfDeactivate() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        // When
        String result = controller.toggleUserStatus(1L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?error=self-deactivate");
    }

    @Test
    void activateUser_success() {
        // When
        String result = controller.activateUser(1L);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?status=activated");
        verify(userService).activateUser(1L);
    }

    @Test
    void deactivateUser_success() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        // When
        String result = controller.deactivateUser(2L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?status=deactivated");
        verify(userService).deactivateUser(2L);
    }

    @Test
    void deactivateUser_selfDeactivate() {
        // Given
        when(principal.getName()).thenReturn("admin@example.com");
        when(userService.findByEmail("admin@example.com")).thenReturn(user);

        // When
        String result = controller.deactivateUser(1L, principal);

        // Then
        assertThat(result).isEqualTo("redirect:/admin/users?error=self-deactivate");
    }
}
