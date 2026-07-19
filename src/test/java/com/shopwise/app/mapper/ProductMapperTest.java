package com.shopwise.app.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.entity.Product;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapCreateRequestToEntity() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("iPhone 15");
        request.setDescription("Apple smartphone");
        request.setCategory("SMARTPHONE");
        request.setPrice(new BigDecimal("999.99"));

        Product product = mapper.toEntity(request);

        assertEquals("iPhone 15", product.getName());
        assertEquals("Apple smartphone", product.getDescription());
        assertEquals("SMARTPHONE", product.getCategory());
        assertEquals(new BigDecimal("999.99"), product.getPrice());
    }

    @Test
    void shouldMapEntityToResponse() {
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setDescription("Apple smartphone");
        product.setCategory("SMARTPHONE");
        product.setPrice(new BigDecimal("999.99"));

        ProductResponse response = mapper.toResponse(product);

        assertEquals(1L, response.getId());
        assertEquals("iPhone 15", response.getName());
        assertEquals("Apple smartphone", response.getDescription());
        assertEquals("SMARTPHONE", response.getCategory());
        assertEquals(new BigDecimal("999.99"), response.getPrice());
    }
}