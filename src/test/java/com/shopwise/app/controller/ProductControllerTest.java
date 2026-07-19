package com.shopwise.app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProductControllerTest {

    @Mock
    private ProductService productService;

    private ProductController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ProductController(productService);
    }

    @Test
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 15");
        request.setDescription("Apple smartphone");
        request.setCategory("SMARTPHONE");
        request.setPrice(new BigDecimal("999.99"));

        ProductResponse response = productResponse(1L, "iPhone 15");

        when(productService.create(request)).thenReturn(response);

        ResponseEntity<ProductResponse> result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("iPhone 15", result.getBody().getName());
    }

    @Test
    void shouldReturnProductById() {
        ProductResponse response = productResponse(1L, "iPhone 15");

        when(productService.getById(1L)).thenReturn(response);

        ResponseEntity<ProductResponse> result = controller.getById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void shouldReturnAllProducts() {
        ProductResponse response1 = productResponse(1L, "iPhone 15");
        ProductResponse response2 = productResponse(2L, "Samsung Galaxy S24");

        when(productService.getAll()).thenReturn(List.of(response1, response2));

        ResponseEntity<List<ProductResponse>> result = controller.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void shouldUpdateProduct() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setDescription("Apple smartphone Pro");
        request.setCategory("SMARTPHONE");
        request.setPrice(new BigDecimal("1399.99"));

        ProductResponse response = productResponse(1L, "iPhone 15 Pro");

        when(productService.update(1L, request)).thenReturn(response);

        ResponseEntity<ProductResponse> result = controller.update(1L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("iPhone 15 Pro", result.getBody().getName());
    }

    @Test
    void shouldDeleteProduct() {
        ResponseEntity<Void> result = controller.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(productService).delete(1L);
    }

    private ProductResponse productResponse(Long id, String name) {
        ProductResponse response = new ProductResponse();
        response.setId(id);
        response.setName(name);
        response.setDescription("Description");
        response.setCategory("SMARTPHONE");
        response.setPrice(new BigDecimal("999.99"));
        return response;
    }
}