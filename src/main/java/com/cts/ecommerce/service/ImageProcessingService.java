package com.cts.ecommerce.service;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageProcessingService {
    
    // Standard dimensions for different use cases
    private static final int PRODUCT_CARD_WIDTH = 400;
    private static final int PRODUCT_CARD_HEIGHT = 300;
    private static final int ADMIN_THUMBNAIL_WIDTH = 100;
    private static final int ADMIN_THUMBNAIL_HEIGHT = 100;
    private static final int PRODUCT_DETAIL_WIDTH = 600;
    private static final int PRODUCT_DETAIL_HEIGHT = 400;
    
    /**
     * Process and resize image for product cards (main display)
     */
    public byte[] processProductCardImage(MultipartFile imageFile) throws IOException {
        return processImage(imageFile, PRODUCT_CARD_WIDTH, PRODUCT_CARD_HEIGHT);
    }
    
    /**
     * Process and resize image for admin thumbnails
     */
    public byte[] processAdminThumbnailImage(MultipartFile imageFile) throws IOException {
        return processImage(imageFile, ADMIN_THUMBNAIL_WIDTH, ADMIN_THUMBNAIL_HEIGHT);
    }
    
    /**
     * Process and resize image for product detail pages
     */
    public byte[] processProductDetailImage(MultipartFile imageFile) throws IOException {
        return processImage(imageFile, PRODUCT_DETAIL_WIDTH, PRODUCT_DETAIL_HEIGHT);
    }
    
    /**
     * Generic image processing method
     */
    private byte[] processImage(MultipartFile imageFile, int targetWidth, int targetHeight) throws IOException {
        try {
            // Read the original image
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageFile.getBytes()));
            
            if (originalImage == null) {
                throw new IOException("Could not read image file");
            }
            
            // Resize the image using imgscalr with high quality
            BufferedImage resizedImage = Scalr.resize(originalImage, 
                Scalr.Method.QUALITY, 
                Scalr.Mode.FIT_EXACT, 
                targetWidth, 
                targetHeight,
                Scalr.OP_ANTIALIAS);
            
            // Convert back to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formatName = getImageFormat(imageFile.getOriginalFilename());
            ImageIO.write(resizedImage, formatName, baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            // If image processing fails, return the original image bytes
            System.err.println("Image processing failed, using original image: " + e.getMessage());
            return imageFile.getBytes();
        }
    }
    
    /**
     * Get image format from filename
     */
    private String getImageFormat(String filename) {
        if (filename == null) return "jpg";
        
        String extension = filename.toLowerCase();
        if (extension.endsWith(".png")) return "png";
        if (extension.endsWith(".gif")) return "gif";
        if (extension.endsWith(".bmp")) return "bmp";
        
        return "jpg"; // default to jpg
    }
    
    /**
     * Get file extension from filename
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
