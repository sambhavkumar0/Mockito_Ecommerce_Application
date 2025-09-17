package com.cts.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded product images
        Path uploadPath = Paths.get("src/main/resources/static/images/products/");
        String uploadDir = uploadPath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
