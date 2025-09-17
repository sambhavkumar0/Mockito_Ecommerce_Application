package com.cts.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageProcessingService imageProcessingService;
    
    // Directory to store uploaded images
    private static final String UPLOAD_DIR = "src/main/resources/static/images/products/";

    /**
     * Saves uploaded image with processing
     * @param imageFile MultipartFile to save
     * @return URL path for the saved image
     * @throws IOException if file operations fail
     */
    public String saveImage(MultipartFile imageFile) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = imageProcessingService.getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Process the image to standard dimensions
        byte[] processedImageBytes = imageProcessingService.processProductCardImage(imageFile);
        
        // Save processed image
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, processedImageBytes);
        
        // Return the URL path for the image (Spring Boot serves static resources from /static automatically)
        return "/images/products/" + uniqueFilename;
    }

    /**
     * Handles image update - either saves new image or keeps existing
     * @param imageFile New image file (can be null/empty)
     * @param existingImageUrl Current image URL
     * @return New image URL or existing URL if no new image provided
     * @throws IOException if file operations fail
     */
    public String handleImageUpdate(MultipartFile imageFile, String existingImageUrl) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // Save new image
            return saveImage(imageFile);
        } else {
            // Keep existing image or return empty string if no existing image
            return existingImageUrl != null ? existingImageUrl : "";
        }
    }
}
