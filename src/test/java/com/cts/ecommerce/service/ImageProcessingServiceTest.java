package com.cts.ecommerce.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ImageProcessingServiceTest {

	private final ImageProcessingService service = new ImageProcessingService();

	@Test
	void processProductCardImage_resizesOrReturnsBytes() throws Exception {
		byte[] png = TestImages.createPngBytes(50, 50);
		MockMultipartFile file = new MockMultipartFile("imageFile", "a.png", "image/png", png);
		byte[] out = service.processProductCardImage(file);
		assertThat(out).isNotEmpty();
	}

	@Test
	void getFileExtension_handlesNullAndValid() {
		assertThat(service.getFileExtension(null)).isEqualTo(".jpg");
		assertThat(service.getFileExtension("x.png")).isEqualTo(".png");
	}

	// Minimal helper to generate a tiny PNG
	static class TestImages {
		static byte[] createPngBytes(int width, int height) throws IOException {
			java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			javax.imageio.ImageIO.write(image, "png", baos);
			return baos.toByteArray();
		}
	}
}
