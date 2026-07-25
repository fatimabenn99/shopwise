package com.shopwise.app.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.RecommendationService;
import com.shopwise.app.service.SaleService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecommendationServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleService saleService;

    @Test
    void shouldReturnRecommendationsWithoutTargetProduct() {
        ProductResponse target = createProduct(
                "Integration Smartphone",
                "Smartphone OLED avec caméra",
                "SMARTPHONE",
                "900.00"
        );

        ProductResponse similar = createProduct(
                "Integration Smartphone Pro",
                "Smartphone OLED premium avec caméra",
                "SMARTPHONE",
                "1100.00"
        );

        ProductResponse different = createProduct(
                "Integration Speaker",
                "Enceinte Bluetooth portable",
                "AUDIO",
                "150.00"
        );

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts(target.getId());

        assertEquals(2, recommendations.size());

        assertTrue(recommendations.stream()
                .noneMatch(recommendation ->
                        recommendation.getProductId()
                                .equals(target.getId())
                ));

        assertTrue(recommendations.stream()
                .anyMatch(recommendation ->
                        recommendation.getProductId()
                                .equals(similar.getId())
                ));

        assertTrue(recommendations.stream()
                .anyMatch(recommendation ->
                        recommendation.getProductId()
                                .equals(different.getId())
                ));
    }

    @Test
    void shouldRankSimilarProductBeforeDifferentProduct() {
        ProductResponse target = createProduct(
                "Samsung Integration Phone",
                "Smartphone Android avec caméra",
                "SMARTPHONE",
                "899.00"
        );

        ProductResponse similar = createProduct(
                "Samsung Integration Phone Ultra",
                "Smartphone Android premium avec caméra",
                "SMARTPHONE",
                "1199.00"
        );

        ProductResponse different = createProduct(
                "Integration Laptop",
                "Ordinateur portable professionnel",
                "LAPTOP",
                "2499.00"
        );

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts(target.getId());

        assertFalse(recommendations.isEmpty());
        assertEquals(similar.getId(), recommendations.get(0).getProductId());

        RecommendationResponse similarRecommendation =
                recommendations.stream()
                        .filter(recommendation ->
                                recommendation.getProductId()
                                        .equals(similar.getId())
                        )
                        .findFirst()
                        .orElseThrow();

        RecommendationResponse differentRecommendation =
                recommendations.stream()
                        .filter(recommendation ->
                                recommendation.getProductId()
                                        .equals(different.getId())
                        )
                        .findFirst()
                        .orElseThrow();

        assertTrue(
                similarRecommendation.getSimilarityScore()
                        >= differentRecommendation.getSimilarityScore()
        );
    }

    @Test
    void shouldUseSalesCoOccurrenceInRecommendations() {
        ProductResponse target = createProduct(
                "CoOccurrence Target",
                "Produit principal",
                "MAIN",
                "100.00"
        );

        ProductResponse frequentlyBoughtWith = createProduct(
                "Frequently Bought Product",
                "Produit acheté avec le produit principal",
                "RELATED",
                "80.00"
        );

        ProductResponse unrelated = createProduct(
                "Unrelated Product",
                "Produit sans historique commun",
                "OTHER",
                "80.00"
        );

        /*
         * Création de plusieurs ventes contenant la cible et le produit associé.
         * Cela alimente les cooccurrences réellement persistées en base
         */
        createSale(target.getId(), frequentlyBoughtWith.getId());
        createSale(target.getId(), frequentlyBoughtWith.getId());
        createSale(target.getId(), frequentlyBoughtWith.getId());

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts(target.getId());

        assertFalse(recommendations.isEmpty());
        assertEquals(
                frequentlyBoughtWith.getId(),
                recommendations.get(0).getProductId()
        );

        RecommendationResponse recommendation =
                recommendations.get(0);

        assertNotNull(recommendation.getReason());
        assertFalse(recommendation.getReason().isBlank());

        assertTrue(recommendations.stream()
                .anyMatch(item ->
                        item.getProductId().equals(unrelated.getId())
                ));
    }

    @Test
    void shouldReturnAtMostFiveRecommendations() {
        ProductResponse target = createProduct(
                "Maximum Recommendations Target",
                "Produit cible",
                "TEST",
                "100.00"
        );

        for (int index = 1; index <= 7; index++) {
            createProduct(
                    "Candidate Product " + index,
                    "Description du candidat " + index,
                    "TEST",
                    String.valueOf(100 + index)
            );
        }

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts(target.getId());

        assertEquals(5, recommendations.size());
    }

    @Test
    void shouldReturnEmptyListWhenOnlyTargetExists() {
        ProductResponse target = createProduct(
                "Only Product",
                "Produit unique",
                "TEST",
                "100.00"
        );

        List<RecommendationResponse> recommendations =
                recommendationService.recommendProducts(target.getId());

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void shouldThrowNotFoundForUnknownTargetProduct() {
        assertThrows(
                NotFoundException.class,
                () -> recommendationService.recommendProducts(999999L)
        );
    }

    private ProductResponse createProduct(
            String name,
            String description,
            String category,
            String price
    ) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setDescription(description);
        request.setCategory(category);
        request.setPrice(new BigDecimal(price));

        return productService.create(request);
    }

    private void createSale(Long firstProductId, Long secondProductId) {
        CreateSaleItemRequest firstItem = new CreateSaleItemRequest();
        firstItem.setProductId(firstProductId);
        firstItem.setQuantity(1);

        CreateSaleItemRequest secondItem = new CreateSaleItemRequest();
        secondItem.setProductId(secondProductId);
        secondItem.setQuantity(1);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setItems(List.of(firstItem, secondItem));

        saleService.create(request);
    }
}