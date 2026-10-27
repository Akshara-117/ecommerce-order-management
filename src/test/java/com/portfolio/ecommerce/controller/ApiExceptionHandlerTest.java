package com.portfolio.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void missingProductReturns404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBusinessError(new IllegalArgumentException("Product not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Product not found", response.getBody().get("message"));
    }

    @Test
    void invalidQuantityReturns400() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBusinessError(new IllegalArgumentException("Quantity must be positive"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    @Test
    void paginationValidationPreserves400AndMessage() {
        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page size"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid page size", response.getBody().get("message"));
    }
}
