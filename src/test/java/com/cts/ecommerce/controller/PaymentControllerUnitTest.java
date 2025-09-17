package com.cts.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentControllerUnitTest {

    @InjectMocks private PaymentController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentController();
    }

    @Test
    void paymentPage_returnsPaymentView() {
        // When
        String view = controller.paymentPage();

        // Then
        assertThat(view).isEqualTo("payment");
    }

    @Test
    void paymentPage_returnsCorrectTemplate() {
        // When
        String result = controller.paymentPage();

        // Then
        assertThat(result).isEqualTo("payment");
        assertThat(result).isNotBlank();
        assertThat(result).contains("payment");
    }
}
