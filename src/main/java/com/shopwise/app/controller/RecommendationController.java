package com.shopwise.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.service.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<RecommendationResponse>> recommendProducts(@PathVariable Long productId) {
        return ResponseEntity.ok(recommendationService.recommendProducts(productId));
    }
}