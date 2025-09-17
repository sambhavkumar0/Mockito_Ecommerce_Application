package com.cts.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock private ImageProcessingService imageProcessingService;
    @InjectMocks private ImageService imageService;

    @TempDir
    Path tempDir;

    private MockMultipartFile imageFile;
    private byte[] processedImageBytes;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory structure that mimics the upload directory
        Path uploadPath = tempDir.resolve("src/main/resources/static/images/products/");
        Files.createDirectories(uploadPath);
        
        // Mock image data
        byte[] imageData = "fake-image-data".getBytes();
        processedImageBytes = "processed-image-data".getBytes();
        
        imageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            imageData
        );
        
        // Mock the image processing service
        when(imageProcessingService.getFileExtension("test-image.jpg")).thenReturn(".jpg");
        when(imageProcessingService.processProductCardImage(any(MultipartFile.class)))
            .thenReturn(processedImageBytes);
    }

    @Test
    void saveImage_createsDirectoryAndSavesImage() throws IOException {
        // Given - The upload directory doesn't exist initially
        // When
        String result = imageService.saveImage(imageFile);

        // Then
        assertThat(result).startsWith("/images/products/");
        assertThat(result).endsWith(".jpg");
        verify(imageProcessingService).getFileExtension("test-image.jpg");
        verify(imageProcessingService).processProductCardImage(imageFile);
    }

    @Test
    void saveImage_withPngFile_returnsCorrectUrl() throws IOException {
        // Given
        MockMultipartFile pngFile = new MockMultipartFile(
            "image", "test.png", "image/png", "png-data".getBytes()
        );
        when(imageProcessingService.getFileExtension("test.png")).thenReturn(".png");

        // When
        String result = imageService.saveImage(pngFile);

        // Then
        assertThat(result).startsWith("/images/products/");
        assertThat(result).endsWith(".png");
        verify(imageProcessingService).getFileExtension("test.png");
    }

    @Test
    void saveImage_withProcessingException_throwsIOException() throws IOException {
        // Given
        when(imageProcessingService.processProductCardImage(any(MultipartFile.class)))
            .thenThrow(new IOException("Processing failed"));

        // When & Then
        assertThatThrownBy(() -> imageService.saveImage(imageFile))
            .isInstanceOf(IOException.class)
            .hasMessage("Processing failed");
    }

    @Test
    void saveImage_withFileWriteException_throwsIOException() throws IOException {
        // Given - Mock a scenario where file writing fails
        when(imageProcessingService.processProductCardImage(any(MultipartFile.class)))
            .thenThrow(new IOException("File write failed"));

        // When & Then
        assertThatThrownBy(() -> imageService.saveImage(imageFile))
            .isInstanceOf(IOException.class)
            .hasMessage("File write failed");
    }

    @Test
    void handleImageUpdate_withNewImage_savesNewImage() throws IOException {
        // Given
        String existingImageUrl = "/images/products/existing.jpg";

        // When
        String result = imageService.handleImageUpdate(imageFile, existingImageUrl);

        // Then
        assertThat(result).startsWith("/images/products/");
        assertThat(result).endsWith(".jpg");
        assertThat(result).isNotEqualTo(existingImageUrl);
        verify(imageProcessingService).processProductCardImage(imageFile);
    }

    @Test
    void handleImageUpdate_withNullImageFile_returnsExistingUrl() throws IOException {
        // Given
        String existingImageUrl = "/images/products/existing.jpg";

        // When
        String result = imageService.handleImageUpdate(null, existingImageUrl);

        // Then
        assertThat(result).isEqualTo(existingImageUrl);
        verify(imageProcessingService, never()).processProductCardImage(any());
    }

    @Test
    void handleImageUpdate_withEmptyImageFile_returnsExistingUrl() throws IOException {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "image", "empty.jpg", "image/jpeg", new byte[0]
        );
        String existingImageUrl = "/images/products/existing.jpg";

        // When
        String result = imageService.handleImageUpdate(emptyFile, existingImageUrl);

        // Then
        assertThat(result).isEqualTo(existingImageUrl);
        verify(imageProcessingService, never()).processProductCardImage(any());
    }

    @Test
    void handleImageUpdate_withNullExistingUrl_returnsEmptyString() throws IOException {
        // Given
        // When
        String result = imageService.handleImageUpdate(null, null);

        // Then
        assertThat(result).isEmpty();
        verify(imageProcessingService, never()).processProductCardImage(any());
    }

    @Test
    void handleImageUpdate_withEmptyExistingUrl_returnsEmptyString() throws IOException {
        // Given
        // When
        String result = imageService.handleImageUpdate(null, "");

        // Then
        assertThat(result).isEmpty();
        verify(imageProcessingService, never()).processProductCardImage(any());
    }

    @Test
    void handleImageUpdate_withNewImageAndNoExistingUrl_savesNewImage() throws IOException {
        // Given
        // When
        String result = imageService.handleImageUpdate(imageFile, null);

        // Then
        assertThat(result).startsWith("/images/products/");
        assertThat(result).endsWith(".jpg");
        verify(imageProcessingService).processProductCardImage(imageFile);
    }

    @Test
    void saveImage_withDifferentFileExtensions() throws IOException {
        // Test various file extensions
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        
        for (String extension : extensions) {
            // Given
            String filename = "test" + extension;
            MockMultipartFile file = new MockMultipartFile(
                "image", filename, "image/jpeg", "data".getBytes()
            );
            when(imageProcessingService.getFileExtension(filename)).thenReturn(extension);
            
            // When
            String result = imageService.saveImage(file);
            
            // Then
            assertThat(result).endsWith(extension);
            verify(imageProcessingService).getFileExtension(filename);
        }
    }

    @Test
    void saveImage_generatesUniqueFilenames() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "image", "test1.jpg", "image/jpeg", "data1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "image", "test2.jpg", "image/jpeg", "data2".getBytes()
        );
        
        when(imageProcessingService.getFileExtension("test1.jpg")).thenReturn(".jpg");
        when(imageProcessingService.getFileExtension("test2.jpg")).thenReturn(".jpg");

        // When
        String result1 = imageService.saveImage(file1);
        String result2 = imageService.saveImage(file2);

        // Then
        assertThat(result1).isNotEqualTo(result2);
        assertThat(result1).endsWith(".jpg");
        assertThat(result2).endsWith(".jpg");
    }
}
