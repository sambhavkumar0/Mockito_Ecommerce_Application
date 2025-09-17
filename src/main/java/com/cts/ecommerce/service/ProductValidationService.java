package com.cts.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
public class ProductValidationService {

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
        "image/jpeg", 
        "image/jpg", 
        "image/png", 
        "image/gif",
        "image/webp"
    );
    
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
        ".jpg", 
        ".jpeg", 
        ".png", 
        ".gif",
        ".webp"
    );

    /**
     * Validates product fields and adds errors to BindingResult
     * @param productName Product name to validate
     * @param description Product description to validate
     * @param price Product price to validate
     * @param stock Product stock to validate
     * @param bindingResult BindingResult to add validation errors
     * @return true if validation passed, false if there are errors
     */
    public boolean validateProductFields(String productName, String description, 
                                       Double price, Integer stock, BindingResult bindingResult) {
        boolean hasErrors = false;
        
        if (productName == null || productName.trim().isEmpty()) {
            bindingResult.rejectValue("name", "NotBlank", "Product name is required");
            hasErrors = true;
        } else if (productName.trim().length() < 2) {
            bindingResult.rejectValue("name", "Size", "Product name must be at least 2 characters long");
            hasErrors = true;
        } else if (productName.trim().length() > 100) {
            bindingResult.rejectValue("name", "Size", "Product name must be no more than 100 characters long");
            hasErrors = true;
        }
        
        if (description == null || description.trim().isEmpty()) {
            bindingResult.rejectValue("description", "NotBlank", "Product description is required");
            hasErrors = true;
        } else if (description.trim().length() < 10) {
            bindingResult.rejectValue("description", "Size", "Product description must be at least 10 characters long");
            hasErrors = true;
        } else if (description.trim().length() > 500) {
            bindingResult.rejectValue("description", "Size", "Product description must be no more than 500 characters long");
            hasErrors = true;
        }
        
        if (price == null || price <= 0) {
            bindingResult.rejectValue("price", "DecimalMin", "Product price must be greater than 0");
            hasErrors = true;
        }
        
        if (stock == null || stock < 0) {
            bindingResult.rejectValue("stock", "Min", "Stock quantity must be 0 or greater");
            hasErrors = true;
        }
        
        return !hasErrors;
    }

    /**
     * Validates image file type and format
     * @param imageFile MultipartFile to validate
     * @return ValidationResult containing validation status and error message
     */
    public ValidationResult validateImageFile(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return new ValidationResult(false, "Product image is required. Please upload an image file.");
        }
        
        String contentType = imageFile.getContentType();
        String fileName = imageFile.getOriginalFilename();
        
        // Check MIME type and file extension
        boolean isValidMimeType = contentType != null && ALLOWED_MIME_TYPES.contains(contentType);
        boolean isValidExtension = fileName != null && ALLOWED_EXTENSIONS.stream()
            .anyMatch(ext -> fileName.toLowerCase().endsWith(ext));
        
        if (!isValidMimeType && !isValidExtension) {
            String debugInfo = String.format("File: %s, MIME: %s", fileName, contentType);
            return new ValidationResult(false, 
                "Please upload a valid image file (JPG, JPEG, PNG, GIF, WEBP). " + debugInfo);
        }
        
        return new ValidationResult(true, null);
    }

    /**
     * Validates that image file is not empty (for updates where image is optional)
     * @param imageFile MultipartFile to validate
     * @return true if image is empty (valid for updates), false if there are issues
     */
    public boolean validateImageFileForUpdate(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return true; // Empty image is valid for updates
        }
        
        ValidationResult result = validateImageFile(imageFile);
        return result.isValid();
    }

    /**
     * Inner class to hold validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
