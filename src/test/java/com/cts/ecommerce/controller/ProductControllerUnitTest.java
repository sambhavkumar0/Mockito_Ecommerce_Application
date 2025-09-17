package com.cts.ecommerce.controller;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.dto.ProductResponse;
import com.cts.ecommerce.mapper.ProductMapper;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerUnitTest {

    @Mock private ProductService productService;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductController controller;

    private Product product;
    private ProductResponse productResponse;
    private ProductRequest productRequest;
    private Model model;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setStock(10);
        product.setActive(true);
        
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setDescription("Test Description");
        productResponse.setPrice(100.0);
        productResponse.setStock(10);
        
        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(100.0);
        productRequest.setStock(10);
        productRequest.setActive(true);
    }

    // ==================== REST API Tests ====================

    @Test
    void listAllApi_returnsActiveProducts() {
        // Given
        List<ProductResponse> products = Arrays.asList(productResponse);
        when(productService.getAllActiveProducts()).thenReturn(products);

        // When
        List<ProductResponse> result = controller.listAllApi();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(productResponse);
        verify(productService).getAllActiveProducts();
    }

    @Test
    void searchProductsApi_withSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchAndFilterProducts("test", null, null)).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = controller.searchProductsApi("test", null, null, null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(productResponse);
        verify(productService).searchAndFilterProducts("test", null, null);
    }

    @Test
    void searchProductsApi_withPriceRange() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchAndFilterProducts(null, 50.0, 150.0)).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = controller.searchProductsApi(null, 50.0, 150.0, null, null);

        // Then
        assertThat(result).hasSize(1);
        verify(productService).searchAndFilterProducts(null, 50.0, 150.0);
    }

    @Test
    void searchProductsApi_withSorting() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getProductsSortedByName("asc")).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = controller.searchProductsApi(null, null, null, "name", "asc");

        // Then
        assertThat(result).hasSize(1);
        verify(productService).getProductsSortedByName("asc");
    }

    @Test
    void searchProductsApi_withPriceSorting() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getProductsSortedByPrice("desc")).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = controller.searchProductsApi(null, null, null, "price", "desc");

        // Then
        assertThat(result).hasSize(1);
        verify(productService).getProductsSortedByPrice("desc");
    }

    @Test
    void getByIdApi_returnsProduct() {
        // Given
        when(productService.getProductById(1L)).thenReturn(productResponse);

        // When
        ProductResponse result = controller.getByIdApi(1L);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productService).getProductById(1L);
    }

    @Test
    void createApi_createsProduct() {
        // Given
        when(productService.createProduct(productRequest)).thenReturn(productResponse);

        // When
        ProductResponse result = controller.createApi(productRequest);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productService).createProduct(productRequest);
    }

    @Test
    void updateApi_updatesProduct() {
        // Given
        when(productService.updateProduct(1L, productRequest)).thenReturn(productResponse);

        // When
        ProductResponse result = controller.updateApi(1L, productRequest);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productService).updateProduct(1L, productRequest);
    }

    @Test
    void deleteApi_deletesProduct() {
        // Given - no return value expected for delete

        // When
        controller.deleteApi(1L);

        // Then
        verify(productService).deleteProduct(1L);
    }

    // ==================== Thymeleaf Page Tests ====================

    @Test
    void productsPage_withoutFilters() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getAllActiveProductsForView()).thenReturn(products);

        // When
        String view = controller.productsPage(null, null, null, null, null, model);

        // Then
        assertThat(view).isEqualTo("products");
        assertThat(model.getAttribute("products")).isEqualTo(products);
        assertThat(model.getAttribute("search")).isNull();
        assertThat(model.getAttribute("minPrice")).isNull();
        assertThat(model.getAttribute("maxPrice")).isNull();
        assertThat(model.getAttribute("sortBy")).isNull();
        assertThat(model.getAttribute("sortOrder")).isNull();
        verify(productService).getAllActiveProductsForView();
    }

    @Test
    void productsPage_withSearchAndFilters() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchAndFilterProducts("test", 50.0, 150.0)).thenReturn(products);

        // When
        String view = controller.productsPage("test", 50.0, 150.0, null, null, model);

        // Then
        assertThat(view).isEqualTo("products");
        assertThat(model.getAttribute("products")).isEqualTo(products);
        assertThat(model.getAttribute("search")).isEqualTo("test");
        assertThat(model.getAttribute("minPrice")).isEqualTo(50.0);
        assertThat(model.getAttribute("maxPrice")).isEqualTo(150.0);
        verify(productService).searchAndFilterProducts("test", 50.0, 150.0);
    }

    @Test
    void productsPage_withNameSorting() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getProductsSortedByName("desc")).thenReturn(products);

        // When
        String view = controller.productsPage(null, null, null, "name", "desc", model);

        // Then
        assertThat(view).isEqualTo("products");
        assertThat(model.getAttribute("sortBy")).isEqualTo("name");
        assertThat(model.getAttribute("sortOrder")).isEqualTo("desc");
        verify(productService).getProductsSortedByName("desc");
    }

    @Test
    void productsPage_withPriceSorting() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getProductsSortedByPrice("asc")).thenReturn(products);

        // When
        String view = controller.productsPage(null, null, null, "price", "asc", model);

        // Then
        assertThat(view).isEqualTo("products");
        assertThat(model.getAttribute("sortBy")).isEqualTo("price");
        assertThat(model.getAttribute("sortOrder")).isEqualTo("asc");
        verify(productService).getProductsSortedByPrice("asc");
    }

    @Test
    void productDetailPage_returnsProduct() {
        // Given
        when(productService.getProductEntityById(1L)).thenReturn(product);

        // When
        String view = controller.productDetailPage(1L, model);

        // Then
        assertThat(view).isEqualTo("product-detail");
        assertThat(model.getAttribute("product")).isEqualTo(product);
        verify(productService).getProductEntityById(1L);
    }

    @Test
    void productDetailPage_withNonExistentProduct() {
        // Given
        when(productService.getProductEntityById(999L))
            .thenThrow(new RuntimeException("Product not found"));

        // When & Then
        try {
            controller.productDetailPage(999L, model);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Product not found");
        }
        verify(productService).getProductEntityById(999L);
    }
}
