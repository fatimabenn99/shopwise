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

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.service.RecommendationService;

class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    private RecommendationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new RecommendationController(recommendationService);
    }

    @Test
    void shouldReturnRecommendations() {

        RecommendationResponse r = new RecommendationResponse();

        r.setProductId(2L);
        r.setProductName("iPhone 15 Plus");
        r.setPrice(new BigDecimal("1199.99"));
        r.setSimilarityScore(0.82);
        r.setReason("Produit proche");

        when(recommendationService.recommendProducts(1L))
                .thenReturn(List.of(r));

        ResponseEntity<List<RecommendationResponse>> result =
                controller.recommendProducts(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals(2L, result.getBody().get(0).getProductId());
    }

}