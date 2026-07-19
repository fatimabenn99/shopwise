package com.shopwise.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.SaleItem;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleItemRepository;
import com.shopwise.app.service.impl.RecommendationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        recommendationService = new RecommendationServiceImpl(productRepository, saleItemRepository);
    }

    @Test
    void shouldReturnRecommendationsWithoutTargetProduct() {
        Product target = product(1L, "iPhone 15", "Apple smartphone OLED", "SMARTPHONE", "999.99");
        Product p2 = product(2L, "iPhone 15 Plus", "Apple smartphone large screen", "SMARTPHONE", "1199.99");
        Product p3 = product(3L, "MacBook Air", "Apple laptop lightweight", "LAPTOP", "1399.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, p2, p3));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertFalse(result.isEmpty());
        assertTrue(result.stream().noneMatch(r -> r.getProductId().equals(1L)));
    }

    @Test
    void shouldReturnMaximumFiveRecommendations() {
        Product target = product(1L, "iPhone 15", "Apple smartphone OLED", "SMARTPHONE", "999.99");

        List<Product> products = List.of(
                target,
                product(2L, "iPhone 15 Plus", "Apple smartphone", "SMARTPHONE", "1199.99"),
                product(3L, "iPhone 15 Pro", "Apple smartphone", "SMARTPHONE", "1399.99"),
                product(4L, "Samsung Galaxy S24", "Android smartphone", "SMARTPHONE", "899.99"),
                product(5L, "Google Pixel 9", "Android smartphone camera", "SMARTPHONE", "949.99"),
                product(6L, "Xiaomi 14", "Android smartphone camera", "SMARTPHONE", "849.99"),
                product(7L, "MacBook Air", "Apple laptop", "LAPTOP", "1399.99")
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(products);
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertTrue(result.size() <= 5);
    }

    @Test
    void shouldSortRecommendationsBySimilarityScoreDescending() {
        Product target = product(1L, "iPhone 15", "Apple smartphone OLED", "SMARTPHONE", "999.99");

        Product close = product(2L, "iPhone 15 Plus", "Apple smartphone OLED large screen", "SMARTPHONE", "1199.99");
        Product far = product(3L, "JBL Charge 5", "Bluetooth speaker portable", "AUDIO", "179.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, far, close));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertTrue(result.size() >= 2);
        assertTrue(result.get(0).getSimilarityScore() >= result.get(1).getSimilarityScore());
    }

    @Test
    void shouldThrowNotFoundWhenTargetProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> recommendationService.recommendProducts(99L));

        verify(productRepository, never()).findAll();
    }

    @Test
    void shouldRecommendSimilarProductFirst() {
        Product target = product(1L, "Samsung Galaxy S24", "Samsung Android smartphone AI camera", "SMARTPHONE", "899.99");

        Product similar = product(2L, "Samsung Galaxy S24 Ultra", "Samsung premium Android smartphone", "SMARTPHONE", "1299.99");
        Product different = product(3L, "MacBook Pro M3", "Apple professional laptop", "LAPTOP", "2499.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, different, similar));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertFalse(result.isEmpty());
        assertEquals(2L, result.get(0).getProductId());
    }

    private Product product(Long id, String name, String description, String category, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        return product;
    }
    
    @Test
    void shouldHandleProductsWithNullCategory() {
        Product target = product(1L, "iPhone 15", "Apple smartphone", null, "999.99");
        Product candidate = product(2L, "iPhone 15 Plus", "Apple smartphone", null, "1199.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, candidate));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldHandleNullDescription() {
        Product target = product(1L, "iPhone 15", null, "SMARTPHONE", "999.99");
        Product candidate = product(2L, "Samsung Galaxy S24", null, "SMARTPHONE", "899.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, candidate));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldHandleSamePriceProducts() {
        Product target = product(1L, "iPhone 15", "Apple smartphone", "SMARTPHONE", "999.99");
        Product candidate = product(2L, "Galaxy S24", "Android smartphone", "SMARTPHONE", "999.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target, candidate));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertFalse(result.isEmpty());
        assertEquals(2L, result.get(0).getProductId());
    }

    @Test
    void shouldReturnEmptyListWhenOnlyTargetProductExists() {
        Product target = product(1L, "iPhone 15", "Apple smartphone", "SMARTPHONE", "999.99");

        when(productRepository.findById(1L)).thenReturn(Optional.of(target));
        when(productRepository.findAll()).thenReturn(List.of(target));
        when(saleItemRepository.findAll()).thenReturn(List.of());

        List<RecommendationResponse> result = recommendationService.recommendProducts(1L);

        assertTrue(result.isEmpty());
    }
}