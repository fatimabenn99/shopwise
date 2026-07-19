package com.shopwise.app.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.service.SaleService;

class SaleControllerTest {

    @Mock
    private SaleService saleService;

    private SaleController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new SaleController(saleService);
    }

    @Test
    void shouldCreateSale() {

        CreateSaleRequest request = new CreateSaleRequest();

        SaleResponse response = new SaleResponse();
        response.setId(1L);
        response.setTotal(new BigDecimal("1499.99"));

        when(saleService.create(request)).thenReturn(response);

        ResponseEntity<SaleResponse> result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(1L, result.getBody().getId());
    }

    @Test
    void shouldReturnAllSales() {

        SaleResponse s1 = new SaleResponse();
        s1.setId(1L);

        SaleResponse s2 = new SaleResponse();
        s2.setId(2L);

        when(saleService.getAll()).thenReturn(List.of(s1, s2));

        ResponseEntity<List<SaleResponse>> result = controller.getAll();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(2, result.getBody().size());
    }

    @Test
    void shouldReturnSaleById() {

        SaleResponse sale = new SaleResponse();
        sale.setId(10L);

        when(saleService.getById(10L)).thenReturn(sale);

        ResponseEntity<SaleResponse> result = controller.getById(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(10L, result.getBody().getId());
    }

}