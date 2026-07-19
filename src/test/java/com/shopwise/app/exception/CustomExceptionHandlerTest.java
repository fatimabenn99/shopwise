package com.shopwise.app.exception;

import static org.junit.jupiter.api.Assertions.*;

import com.shopwise.app.dto.response.ErrorResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CustomExceptionHandlerTest {

    private CustomExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CustomExceptionHandler();
    }

    @Test
    void shouldHandleNotFoundException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new NotFoundException("Product not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("Product not found", response.getBody().getMessage());
    }

    @Test
    void shouldHandleUnauthorizedException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorized(new UnauthorizedException("Invalid credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void shouldHandleConflictException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleConflict(new ConflictException("Username already exists"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getCode());
        assertEquals("Username already exists", response.getBody().getMessage());
    }

    @Test
    void shouldHandleGenericException() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAll(new RuntimeException("Unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}