package com.cts.ecommerce.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeControllerUnitTest {

    @InjectMocks private HomeController controller;
    
    private Model model;
    private Principal principal;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
    }

    @Test
    void landingPage_withAuthenticatedUser_returnsLandingWithUsername() {
        // Given
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john@example.com");

        // When
        String view = controller.landingPage(model, principal);

        // Then
        assertThat(view).isEqualTo("landing");
        assertThat(model.getAttribute("username")).isEqualTo("john@example.com");
    }

    @Test
    void landingPage_withoutAuthentication_returnsLandingWithoutUsername() {
        // Given
        principal = null;

        // When
        String view = controller.landingPage(model, principal);

        // Then
        assertThat(view).isEqualTo("landing");
        assertThat(model.getAttribute("username")).isNull();
    }

    @Test
    void landingPage_withNullPrincipal_returnsLandingWithoutUsername() {
        // Given
        principal = null;

        // When
        String view = controller.landingPage(model, principal);

        // Then
        assertThat(view).isEqualTo("landing");
        assertThat(model.getAttribute("username")).isNull();
    }
}
