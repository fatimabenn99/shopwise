package com.shopwise.app.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

class SaleMapperTest {

    private final SaleMapper mapper = Mappers.getMapper(SaleMapper.class);

    @Test
    void shouldMapSaleToResponse() {
        Product product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");

        Sale sale = new Sale();
        sale.setId(10L);
        sale.setTotal(new BigDecimal("999.99"));

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("999.99"));
        item.setLineTotal(new BigDecimal("999.99"));

        sale.addItem(item);

        SaleResponse response = mapper.toResponse(sale);

        assertEquals(10L, response.getId());
        assertEquals(new BigDecimal("999.99"), response.getTotal());
        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals("iPhone 15", response.getItems().get(0).getProductName());
    }
}