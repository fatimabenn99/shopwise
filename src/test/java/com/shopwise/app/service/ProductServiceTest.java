package com.shopwise.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.ProductMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.service.impl.ProductServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductServiceImpl(productRepository, productMapper);
    }

    @Test
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 15");
        request.setDescription("Apple smartphone");
        request.setCategory("SMARTPHONE");
        request.setPrice(new BigDecimal("999.99"));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());

        Product saved = new Product();
        saved.setId(1L);
        saved.setName(request.getName());
        saved.setDescription(request.getDescription());
        saved.setCategory(request.getCategory());
        saved.setPrice(request.getPrice());

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("iPhone 15");
        response.setDescription("Apple smartphone");
        response.setCategory("SMARTPHONE");
        response.setPrice(new BigDecimal("999.99"));

        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
        assertEquals("SMARTPHONE", result.getCategory());
        verify(productRepository).save(product);
    }

    @Test
    void shouldReturnProductById() {
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setCategory("SMARTPHONE");

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("iPhone 15");
        response.setCategory("SMARTPHONE");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getById(1L);

        assertEquals(1L, result.getId());
        assertEquals("iPhone 15", result.getName());
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getById(99L));
    }

    @Test
    void shouldReturnAllProducts() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung Galaxy S24");

        ProductResponse response1 = new ProductResponse();
        response1.setId(1L);
        response1.setName("iPhone 15");

        ProductResponse response2 = new ProductResponse();
        response2.setId(2L);
        response2.setName("Samsung Galaxy S24");

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));
        when(productMapper.toResponseList(List.of(product1, product2)))
                .thenReturn(List.of(response1, response2));

        List<ProductResponse> result = productService.getAll();

        assertEquals(2, result.size());
        assertEquals("iPhone 15", result.get(0).getName());
    }

    @Test
    void shouldUpdateProduct() {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("iPhone 15 Pro");
        request.setDescription("Apple smartphone Pro");
        request.setCategory("SMARTPHONE");
        request.setPrice(new BigDecimal("1399.99"));

        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");

        Product updated = new Product();
        updated.setId(1L);
        updated.setName("iPhone 15 Pro");
        updated.setCategory("SMARTPHONE");
        updated.setPrice(new BigDecimal("1399.99"));

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("iPhone 15 Pro");
        response.setCategory("SMARTPHONE");
        response.setPrice(new BigDecimal("1399.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(updated);
        when(productMapper.toResponse(updated)).thenReturn(response);

        ProductResponse result = productService.update(1L, request);

        assertEquals("iPhone 15 Pro", result.getName());
        verify(productMapper).updateEntity(request, product);
        verify(productRepository).save(product);
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingUnknownProduct() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> productService.delete(99L));
        verify(productRepository, never()).deleteById(99L);
    }
}