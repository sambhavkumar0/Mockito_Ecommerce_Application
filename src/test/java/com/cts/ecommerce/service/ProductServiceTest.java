package com.cts.ecommerce.service;

import com.cts.ecommerce.dto.ProductRequest;
import com.cts.ecommerce.dto.ProductResponse;
import com.cts.ecommerce.mapper.ProductMapper;
import com.cts.ecommerce.model.Product;
import com.cts.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private Product product;
    private ProductResponse productResponse;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(100.0);
        product.setStock(10);
        product.setActive(true);
        product.setImageUrl("test-image.jpg");

        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setDescription("Test Description");
        productResponse.setPrice(100.0);
        productResponse.setStock(10);
        productResponse.setImageUrl("test-image.jpg");

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(100.0);
        productRequest.setStock(10);
        productRequest.setActive(true);
        productRequest.setImageUrl("test-image.jpg");
    }

    @Test
    void getAllActiveProducts_returnsActiveProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.getAllActiveProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(productResponse);
        verify(productRepository).findByActiveTrue();
        verify(productMapper).toDto(product);
    }

    @Test
    void getAllActiveProductsForView_returnsActiveProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.getAllActiveProductsForView();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductById_returnsProductResponse() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.getProductById(1L);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productRepository).findById(1L);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProductById_notFound_throwsException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.getProductById(999L));
        
        assertThat(exception.getMessage()).isEqualTo("Product not found with id 999");
        verify(productRepository).findById(999L);
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    void getProductEntityById_returnsProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        Product result = productService.getProductEntityById(1L);

        // Then
        assertThat(result).isEqualTo(product);
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductEntityById_notFound_throwsException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.getProductEntityById(999L));
        
        assertThat(exception.getMessage()).isEqualTo("Product not found with id 999");
        verify(productRepository).findById(999L);
    }

    @Test
    void createProduct_savesAndReturnsResponse() {
        // Given
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.createProduct(productRequest);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productMapper).toEntity(productRequest);
        verify(productRepository).save(product);
        verify(productMapper).toDto(product);
    }

    @Test
    void updateProduct_updatesAndReturnsResponse() {
        // Given
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Old Name");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(productMapper.toDto(existingProduct)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.updateProduct(1L, productRequest);

        // Then
        assertThat(result).isEqualTo(productResponse);
        verify(productRepository).findById(1L);
        verify(productMapper).updateEntity(productRequest, existingProduct);
        verify(productRepository).save(existingProduct);
        verify(productMapper).toDto(existingProduct);
    }

    @Test
    void updateProduct_notFound_throwsException() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.updateProduct(999L, productRequest));
        
        assertThat(exception.getMessage()).isEqualTo("Product not found with id 999");
        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_deletesProduct() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> productService.deleteProduct(999L));
        
        assertThat(exception.getMessage()).isEqualTo("Product not found with id 999");
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllProducts_returnsAllProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findAll();
    }

    @Test
    void softDeleteProduct_deactivatesProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        productService.softDeleteProduct(1L);

        // Then
        assertThat(product.isActive()).isFalse();
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void softDeleteProduct_notFound_doesNothing() {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        productService.softDeleteProduct(999L);

        // Then
        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchProductsByName_withSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrueAndNameContainingIgnoreCase("test")).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName("test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrueAndNameContainingIgnoreCase("test");
    }

    @Test
    void searchProductsByName_emptySearchTerm_returnsAllActive() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName("");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue(); // ✅ Correct
        verify(productRepository, never()).findByActiveTrueAndNameContainingIgnoreCase(anyString());
    }


    @Test
    void searchProductsByName_nullSearchTerm_returnsAllActive() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.searchProductsByName(null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void filterProductsByPriceRange_minPriceOnly() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrueAndPriceGreaterThanEqual(50.0)).thenReturn(products);

        // When
        List<Product> result = productService.filterProductsByPriceRange(50.0, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrueAndPriceGreaterThanEqual(50.0);
    }

    @Test
    void filterProductsByPriceRange_maxPriceOnly() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrueAndPriceLessThanEqual(150.0)).thenReturn(products);

        // When
        List<Product> result = productService.filterProductsByPriceRange(null, 150.0);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrueAndPriceLessThanEqual(150.0);
    }

    @Test
    void filterProductsByPriceRange_bothPrices() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrueAndPriceBetween(50.0, 150.0)).thenReturn(products);

        // When
        List<Product> result = productService.filterProductsByPriceRange(50.0, 150.0);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrueAndPriceBetween(50.0, 150.0);
    }

    @Test
    void filterProductsByPriceRange_noPrices_returnsAllActive() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.filterProductsByPriceRange(null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void searchAndFilterProducts_withSearchTerm() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.searchAndFilterProducts("test", null, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void searchAndFilterProducts_withPriceFilters() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.searchAndFilterProducts(null, 50.0, 150.0);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(product);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void searchAndFilterProducts_filtersOutLowPrice() {
        // Given
        Product expensiveProduct = new Product();
        expensiveProduct.setPrice(200.0);
        expensiveProduct.setName("Expensive Product");
        
        List<Product> products = Arrays.asList(product, expensiveProduct);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.searchAndFilterProducts(null, 150.0, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(expensiveProduct);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsSortedByName_ascending() {
        // Given
        Product productA = new Product();
        productA.setName("A Product");
        Product productB = new Product();
        productB.setName("B Product");
        
        List<Product> products = Arrays.asList(productB, productA);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.getProductsSortedByName("asc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A Product");
        assertThat(result.get(1).getName()).isEqualTo("B Product");
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsSortedByName_descending() {
        // Given
        Product productA = new Product();
        productA.setName("A Product");
        Product productB = new Product();
        productB.setName("B Product");
        
        List<Product> products = Arrays.asList(productA, productB);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.getProductsSortedByName("desc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("B Product");
        assertThat(result.get(1).getName()).isEqualTo("A Product");
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsSortedByPrice_ascending() {
        // Given
        Product cheapProduct = new Product();
        cheapProduct.setPrice(50.0);
        Product expensiveProduct = new Product();
        expensiveProduct.setPrice(100.0);
        
        List<Product> products = Arrays.asList(expensiveProduct, cheapProduct);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.getProductsSortedByPrice("asc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(50.0);
        assertThat(result.get(1).getPrice()).isEqualTo(100.0);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsSortedByPrice_descending() {
        // Given
        Product cheapProduct = new Product();
        cheapProduct.setPrice(50.0);
        Product expensiveProduct = new Product();
        expensiveProduct.setPrice(100.0);
        
        List<Product> products = Arrays.asList(cheapProduct, expensiveProduct);
        when(productRepository.findByActiveTrue()).thenReturn(products);

        // When
        List<Product> result = productService.getProductsSortedByPrice("desc");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(100.0);
        assertThat(result.get(1).getPrice()).isEqualTo(50.0);
        verify(productRepository).findByActiveTrue();
    }
}
