package com.cts.ecommerce.exception;

import com.cts.ecommerce.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified global exception handler for both web and API controllers
 * Handles all exceptions and returns appropriate response based on request type
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Check if the request is for an API endpoint
     */
    private boolean isApiRequest(WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        return path.startsWith("/api/") || path.contains("/api/");
    }

    /**
     * Handle validation errors for both web and API endpoints
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request, Model model) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Validation failed", errors);
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.badRequest().body(response);
        } else {
            model.addAttribute("validationErrors", errors);
            model.addAttribute("error", "Please correct the errors below");
            return "error/validation";
        }
    }

    /**
     * Handle constraint violation exceptions for both web and API endpoints
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Validation failed");
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.badRequest().body(response);
        } else {
            model.addAttribute("error", "Validation failed: " + ex.getMessage());
            return "error/validation";
        }
    }

    /**
     * Handle 404 errors for unknown URLs
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("The page you're looking for doesn't exist.");
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            model.addAttribute("error", "The page you're looking for doesn't exist.");
            return "error/404";
        }
    }

    /**
     * Handle resource not found exceptions (Spring 6+)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Page not found: " + ex.getResourcePath());
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            model.addAttribute("error", "Page not found: " + ex.getResourcePath());
            return "error/404";
        }
    }

    /**
     * Handle entity not found exceptions for both API and web endpoints
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFound(EntityNotFoundException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Resource not found: " + ex.getMessage());
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            model.addAttribute("error", "Resource not found: " + ex.getMessage());
            return "error/404";
        }
    }

    /**
     * Handle illegal state exceptions (business logic errors) for both web and API endpoints
     */
    @ExceptionHandler(IllegalStateException.class)
    public Object handleIllegalState(IllegalStateException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Operation not allowed: " + ex.getMessage());
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            model.addAttribute("error", "Operation not allowed: " + ex.getMessage());
            return "error/validation";
        }
    }

    /**
     * Handle illegal argument exceptions for both web and API endpoints
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, WebRequest request, Model model) {
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("Invalid argument: " + ex.getMessage());
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.badRequest().body(response);
        } else {
            model.addAttribute("error", "Invalid input: " + ex.getMessage());
            return "error/validation";
        }
    }

    /**
     * Handle runtime exceptions for both web and API endpoints
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, WebRequest request, Model model) {
        // Don't handle exceptions in redirect scenarios
        String path = request.getDescription(false).replace("uri=", "");
        if (path.contains("/delete") || path.contains("redirect:")) {
            return null; // Let the controller handle it
        }
        
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("An error occurred: " + ex.getMessage());
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            model.addAttribute("error", "An error occurred: " + ex.getMessage());
            return "error/500";
        }
    }

    /**
     * Handle general exceptions for both web and API endpoints
     */
    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, WebRequest request, Model model) {
        // Don't handle exceptions in redirect scenarios
        String path = request.getDescription(false).replace("uri=", "");
        if (path.contains("/delete") || path.contains("redirect:")) {
            return null; // Let the controller handle it
        }
        
        if (isApiRequest(request)) {
            ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred. Please try again later.");
            response.setPath(request.getDescription(false).replace("uri=", ""));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
            return "error/500";
        }
    }

}
