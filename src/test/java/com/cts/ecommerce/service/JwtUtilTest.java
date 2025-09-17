package com.cts.ecommerce.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

	@Test
	void generateAndValidateToken_roundTrip() {
		JwtUtil jwtUtil = new JwtUtil();
		// Use reflection to set private fields annotated with @Value
		setField(jwtUtil, "jwtSecret", "test-secret-key-12345678901234567890");
		setField(jwtUtil, "jwtExpirationMs", 3600000); // 1 hour

		String token = jwtUtil.generateToken("user@example.com");
		assertThat(jwtUtil.validateToken(token)).isTrue();
		assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("user@example.com");
	}

	private static void setField(Object target, String fieldName, Object value) {
		try {
			var field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
