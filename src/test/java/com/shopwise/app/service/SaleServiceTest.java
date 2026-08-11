package com.shopwise.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.request.UpdateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.SaleMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;
import com.shopwise.app.service.impl.SaleServiceImpl;

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
        assertBigDecimalEquals("2279.97", result.getTotal());

        verify(productRepository).findById(1L);
        verify(productRepository).findById(2L);
        verify(saleRepository).save(any(Sale.class));
        verify(saleMapper).toResponse(savedSale);
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

        verify(productRepository).findById(99L);
        verify(saleRepository, never()).save(any(Sale.class));
        verifyNoInteractions(saleMapper);
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

        List<Sale> sales = List.of(sale2, sale1);
        List<SaleResponse> responses = List.of(response2, response1);

        when(saleRepository.findAllByOrderBySaleDateDesc())
                .thenReturn(sales);

        when(saleMapper.toResponseList(sales))
                .thenReturn(responses);

        List<SaleResponse> result = saleService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());

        verify(saleRepository).findAllByOrderBySaleDateDesc();
        verify(saleMapper).toResponseList(sales);
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

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(saleRepository).findById(1L);
        verify(saleMapper).toResponse(sale);
    }

    @Test
    void shouldThrowNotFoundWhenSaleDoesNotExist() {
        when(saleRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> saleService.getById(99L));

        verify(saleRepository).findById(99L);
        verifyNoInteractions(saleMapper);
    }

    @Test
    void shouldUpdateSaleAndRecalculateTotal() {
        Product oldProduct = new Product();
        oldProduct.setId(10L);
        oldProduct.setName("Old Product");
        oldProduct.setPrice(new BigDecimal("50.00"));

        SaleItem oldItem = new SaleItem();
        oldItem.setProduct(oldProduct);
        oldItem.setQuantity(1);
        oldItem.setUnitPrice(new BigDecimal("50.00"));
        oldItem.setLineTotal(new BigDecimal("50.00"));

        Sale existingSale = new Sale();
        existingSale.setId(5L);
        existingSale.setTotal(new BigDecimal("50.00"));
        existingSale.addItem(oldItem);

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone 15");
        product1.setPrice(new BigDecimal("999.99"));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("AirPods Pro");
        product2.setPrice(new BigDecimal("279.99"));

        CreateSaleItemRequest newItem1 = new CreateSaleItemRequest();
        newItem1.setProductId(1L);
        newItem1.setQuantity(2);

        CreateSaleItemRequest newItem2 = new CreateSaleItemRequest();
        newItem2.setProductId(2L);
        newItem2.setQuantity(1);

        UpdateSaleRequest request = new UpdateSaleRequest();
        request.setItems(List.of(newItem1, newItem2));

        SaleResponse expectedResponse = new SaleResponse();
        expectedResponse.setId(5L);
        expectedResponse.setTotal(new BigDecimal("2279.97"));

        when(saleRepository.findById(5L))
                .thenReturn(Optional.of(existingSale));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product1));

        when(productRepository.findById(2L))
                .thenReturn(Optional.of(product2));

        when(saleRepository.save(any(Sale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(saleMapper.toResponse(any(Sale.class)))
                .thenReturn(expectedResponse);

        SaleResponse result = saleService.update(5L, request);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertBigDecimalEquals("2279.97", result.getTotal());

        ArgumentCaptor<Sale> saleCaptor =
                ArgumentCaptor.forClass(Sale.class);

        verify(saleRepository).save(saleCaptor.capture());

        Sale updatedSale = saleCaptor.getValue();

        assertEquals(5L, updatedSale.getId());

        assertBigDecimalEquals(
                "2279.97",
                updatedSale.getTotal()
        );

        assertNotNull(updatedSale.getItems());
        assertEquals(2, updatedSale.getItems().size());

        SaleItem updatedItem1 = updatedSale.getItems().stream()
                .filter(item ->
                        item.getProduct().getId().equals(1L)
                )
                .findFirst()
                .orElseThrow();

        assertEquals(2, updatedItem1.getQuantity());

        assertBigDecimalEquals(
                "999.99",
                updatedItem1.getUnitPrice()
        );

        assertBigDecimalEquals(
                "1999.98",
                updatedItem1.getLineTotal()
        );

        assertSame(existingSale, updatedItem1.getSale());

        SaleItem updatedItem2 = updatedSale.getItems().stream()
                .filter(item ->
                        item.getProduct().getId().equals(2L)
                )
                .findFirst()
                .orElseThrow();

        assertEquals(1, updatedItem2.getQuantity());

        assertBigDecimalEquals(
                "279.99",
                updatedItem2.getUnitPrice()
        );

        assertBigDecimalEquals(
                "279.99",
                updatedItem2.getLineTotal()
        );

        assertSame(existingSale, updatedItem2.getSale());

        assertFalse(
                updatedSale.getItems().stream()
                        .anyMatch(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(10L)
                        )
        );

        verify(saleRepository).findById(5L);
        verify(productRepository).findById(1L);
        verify(productRepository).findById(2L);
        verify(saleMapper).toResponse(updatedSale);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingUnknownSale() {
        CreateSaleItemRequest item = new CreateSaleItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);

        UpdateSaleRequest request = new UpdateSaleRequest();
        request.setItems(List.of(item));

        when(saleRepository.findById(999L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> saleService.update(999L, request)
        );

        assertEquals(
                "Sale not found",
                exception.getMessage()
        );

        verify(saleRepository).findById(999L);
        verifyNoInteractions(productRepository);
        verify(saleRepository, never()).save(any(Sale.class));
        verifyNoInteractions(saleMapper);
    }

    @Test
    void shouldThrowNotFoundWhenProductDoesNotExistDuringSaleUpdate() {
        Sale existingSale = new Sale();
        existingSale.setId(5L);
        existingSale.setTotal(new BigDecimal("50.00"));

        CreateSaleItemRequest item = new CreateSaleItemRequest();
        item.setProductId(999L);
        item.setQuantity(2);

        UpdateSaleRequest request = new UpdateSaleRequest();
        request.setItems(List.of(item));

        when(saleRepository.findById(5L))
                .thenReturn(Optional.of(existingSale));

        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> saleService.update(5L, request)
        );

        assertEquals(
                "Product not found",
                exception.getMessage()
        );

        verify(saleRepository).findById(5L);
        verify(productRepository).findById(999L);
        verify(saleRepository, never()).save(any(Sale.class));
        verifyNoInteractions(saleMapper);
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);

        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}