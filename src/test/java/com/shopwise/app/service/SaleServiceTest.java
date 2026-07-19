package com.shopwise.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.SaleMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;
import com.shopwise.app.service.impl.SaleServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleMapper saleMapper;

    private SaleService saleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        saleService = new SaleServiceImpl(saleRepository, productRepository, saleMapper);
    }

    @Test
    void shouldCreateSaleAndCalculateTotal() {
        CreateSaleItemRequest item1 = new CreateSaleItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        CreateSaleItemRequest item2 = new CreateSaleItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item1, item2));

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setPrice(new BigDecimal("999.99"));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("AirPods Pro");
        product2.setPrice(new BigDecimal("279.99"));

        Sale savedSale = new Sale();
        savedSale.setId(1L);
        savedSale.setTotal(new BigDecimal("2279.97"));

        SaleResponse response = new SaleResponse();
        response.setId(1L);
        response.setTotal(new BigDecimal("2279.97"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);
        when(saleMapper.toResponse(savedSale)).thenReturn(response);

        SaleResponse result = saleService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("2279.97"), result.getTotal());

        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExistDuringSaleCreation() {
        CreateSaleItemRequest item = new CreateSaleItemRequest();
        item.setProductId(99L);
        item.setQuantity(1);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(item));

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> saleService.create(request));

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void shouldReturnAllSales() {
        Sale sale1 = new Sale();
        sale1.setId(1L);

        Sale sale2 = new Sale();
        sale2.setId(2L);

        SaleResponse response1 = new SaleResponse();
        response1.setId(1L);

        SaleResponse response2 = new SaleResponse();
        response2.setId(2L);

        when(saleRepository.findAllByOrderBySaleDateDesc()).thenReturn(List.of(sale2, sale1));
        when(saleMapper.toResponseList(List.of(sale2, sale1))).thenReturn(List.of(response2, response1));

        List<SaleResponse> result = saleService.getAll();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
    }

    @Test
    void shouldReturnSaleById() {
        Sale sale = new Sale();
        sale.setId(1L);

        SaleResponse response = new SaleResponse();
        response.setId(1L);

        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(saleMapper.toResponse(sale)).thenReturn(response);

        SaleResponse result = saleService.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenSaleDoesNotExist() {
        when(saleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> saleService.getById(99L));
    }
}