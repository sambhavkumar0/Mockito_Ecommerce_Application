package com.cts.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductValidationServiceTest {

    @InjectMocks private ProductValidationService validationService;

    private BindingResult bindingResult;
    private MultipartFile imageFile;

    @BeforeEach
    void setUp() {
        bindingResult = mock(BindingResult.class);
    }

    // Test validateProductFields method
    @Test
    void validateProductFields_withValidData_returnsTrue() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isTrue();
        verify(bindingResult, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    void validateProductFields_withEmptyName_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "", "Valid Description", 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("name", "NotBlank", "Product name is required");
    }

    @Test
    void validateProductFields_withNullName_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            null, "Valid Description", 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("name", "NotBlank", "Product name is required");
    }

    @Test
    void validateProductFields_withWhitespaceName_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "   ", "Valid Description", 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("name", "NotBlank", "Product name is required");
    }

    @Test
    void validateProductFields_withEmptyDescription_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "", 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("description", "NotBlank", "Product description is required");
    }

    @Test
    void validateProductFields_withNullDescription_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", null, 99.99, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("description", "NotBlank", "Product description is required");
    }

    @Test
    void validateProductFields_withZeroPrice_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", 0.0, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("price", "DecimalMin", "Product price must be greater than 0");
    }

    @Test
    void validateProductFields_withNegativePrice_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", -10.0, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("price", "DecimalMin", "Product price must be greater than 0");
    }

    @Test
    void validateProductFields_withNullPrice_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", null, 10, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("price", "DecimalMin", "Product price must be greater than 0");
    }

    @Test
    void validateProductFields_withNegativeStock_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", 99.99, -1, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("stock", "Min", "Stock quantity must be 0 or greater");
    }

    @Test
    void validateProductFields_withNullStock_addsError() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", 99.99, null, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("stock", "Min", "Stock quantity must be 0 or greater");
    }

    @Test
    void validateProductFields_withZeroStock_returnsTrue() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(false);

        // When
        boolean result = validationService.validateProductFields(
            "Valid Product", "Valid Description", 99.99, 0, bindingResult
        );

        // Then
        assertThat(result).isTrue();
        verify(bindingResult, never()).rejectValue(anyString(), anyString(), anyString());
    }

    @Test
    void validateProductFields_withMultipleErrors_addsAllErrors() {
        // Given
        when(bindingResult.hasErrors()).thenReturn(true);

        // When
        boolean result = validationService.validateProductFields(
            "", "", -10.0, -5, bindingResult
        );

        // Then
        assertThat(result).isFalse();
        verify(bindingResult).rejectValue("name", "NotBlank", "Product name is required");
        verify(bindingResult).rejectValue("description", "NotBlank", "Product description is required");
        verify(bindingResult).rejectValue("price", "DecimalMin", "Product price must be greater than 0");
        verify(bindingResult).rejectValue("stock", "Min", "Stock quantity must be 0 or greater");
    }

    // Test validateImageFile method
    @Test
    void validateImageFile_withValidJpeg_returnsValidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateImageFile_withValidPng_returnsValidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.png", "image/png", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateImageFile_withValidGif_returnsValidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.gif", "image/gif", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateImageFile_withValidWebp_returnsValidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.webp", "image/webp", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateImageFile_withEmptyFile_returnsInvalidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", new byte[0]
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Product image is required. Please upload an image file.");
    }

    @Test
    void validateImageFile_withInvalidMimeType_returnsInvalidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.txt", "text/plain", "text-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Please upload a valid image file");
        assertThat(result.getErrorMessage()).contains("test.txt");
        assertThat(result.getErrorMessage()).contains("text/plain");
    }

    @Test
    void validateImageFile_withInvalidExtension_returnsInvalidResult() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.pdf", "application/pdf", "pdf-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Please upload a valid image file");
        assertThat(result.getErrorMessage()).contains("test.pdf");
        assertThat(result.getErrorMessage()).contains("application/pdf");
    }

    @Test
    void validateImageFile_withValidExtensionButInvalidMimeType_returnsValidResult() {
        // Given - This tests the OR logic: valid extension OR valid MIME type
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "application/octet-stream", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateImageFile_withValidMimeTypeButInvalidExtension_returnsValidResult() {
        // Given - This tests the OR logic: valid extension OR valid MIME type
        imageFile = new MockMultipartFile(
            "image", "test.unknown", "image/jpeg", "image-data".getBytes()
        );

        // When
        ProductValidationService.ValidationResult result = validationService.validateImageFile(imageFile);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    // Test validateImageFileForUpdate method
    @Test
    void validateImageFileForUpdate_withNullFile_returnsTrue() {
        // When
        boolean result = validationService.validateImageFileForUpdate(null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateImageFileForUpdate_withEmptyFile_returnsTrue() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", new byte[0]
        );

        // When
        boolean result = validationService.validateImageFileForUpdate(imageFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateImageFileForUpdate_withValidFile_returnsTrue() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "image-data".getBytes()
        );

        // When
        boolean result = validationService.validateImageFileForUpdate(imageFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void validateImageFileForUpdate_withInvalidFile_returnsFalse() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.txt", "text/plain", "text-data".getBytes()
        );

        // When
        boolean result = validationService.validateImageFileForUpdate(imageFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void validateImageFileForUpdate_withValidFileButEmpty_returnsTrue() {
        // Given
        imageFile = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", new byte[0]
        );

        // When
        boolean result = validationService.validateImageFileForUpdate(imageFile);

        // Then
        assertThat(result).isTrue();
    }
}
