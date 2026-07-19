package com.shopwise.app.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleItemRepository;
import com.shopwise.app.service.RecommendationService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private static final int K_NEIGHBORS = 5;

    private static final double CATEGORY_WEIGHT = 0.35;
    private static final double TEXT_WEIGHT = 0.30;
    private static final double PRICE_WEIGHT = 0.20;
    private static final double SALES_WEIGHT = 0.15;

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "latest", "new",
            "de", "du", "des", "le", "la", "les", "un", "une",
            "avec", "pour", "dans", "sur", "par"
    );

    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;

    public RecommendationServiceImpl(
            ProductRepository productRepository,
            SaleItemRepository saleItemRepository
    ) {
        this.productRepository = productRepository;
        this.saleItemRepository = saleItemRepository;
    }

    @Override
    public List<RecommendationResponse> recommendProducts(Long productId) {
        Product target = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        List<Product> products = productRepository.findAll();
        List<SaleItem> saleItems = saleItemRepository.findAll();

        Map<Long, Long> coOccurrences = computeCoOccurrences(productId, saleItems);
        long maxCoOccurrence = coOccurrences.values()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        return products.stream()
                .filter(candidate -> !candidate.getId().equals(productId))
                .map(candidate -> buildKnnRecommendation(target, candidate, coOccurrences, maxCoOccurrence))
                .sorted(Comparator.comparingDouble(RecommendationResponse::getSimilarityScore).reversed())
                .limit(K_NEIGHBORS)
                .toList();
    }

    private RecommendationResponse buildKnnRecommendation(
            Product target,
            Product candidate,
            Map<Long, Long> coOccurrences,
            long maxCoOccurrence
    ) {
        double categoryDistance = computeCategoryDistance(target, candidate);
        double textDistance = computeTextDistance(target, candidate);
        double priceDistance = computePriceDistance(target.getPrice(), candidate.getPrice());
        double salesDistance = computeSalesDistance(candidate.getId(), coOccurrences, maxCoOccurrence);

        double distance =
                (categoryDistance * CATEGORY_WEIGHT)
                        + (textDistance * TEXT_WEIGHT)
                        + (priceDistance * PRICE_WEIGHT)
                        + (salesDistance * SALES_WEIGHT);

        double similarity = 1.0 / (1.0 + distance);
        similarity = round(similarity);

        return new RecommendationResponse(
                candidate.getId(),
                candidate.getName(),
                candidate.getPrice(),
                similarity,
                buildReason(categoryDistance, textDistance, priceDistance, salesDistance)
        );
    }

    private double computeCategoryDistance(Product target, Product candidate) {
        if (target.getCategory() == null || candidate.getCategory() == null) {
            return 1.0;
        }

        return target.getCategory().equalsIgnoreCase(candidate.getCategory()) ? 0.0 : 1.0;
    }

    private double computeTextDistance(Product target, Product candidate) {
        Set<String> targetTokens = tokenize(
                target.getName() + " " + target.getDescription() + " " + target.getCategory()
        );

        Set<String> candidateTokens = tokenize(
                candidate.getName() + " " + candidate.getDescription() + " " + candidate.getCategory()
        );

        if (targetTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 1.0;
        }

        Set<String> intersection = new HashSet<>(targetTokens);
        intersection.retainAll(candidateTokens);

        Set<String> union = new HashSet<>(targetTokens);
        union.addAll(candidateTokens);

        double jaccardSimilarity = (double) intersection.size() / union.size();

        return 1.0 - jaccardSimilarity;
    }

    private double computePriceDistance(BigDecimal targetPrice, BigDecimal candidatePrice) {
        if (targetPrice == null || candidatePrice == null) {
            return 1.0;
        }

        BigDecimal max = targetPrice.max(candidatePrice);

        if (max.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal difference = targetPrice.subtract(candidatePrice).abs();

        BigDecimal distance = difference.divide(max, 4, RoundingMode.HALF_UP);

        return Math.min(1.0, distance.doubleValue());
    }

    private Map<Long, Long> computeCoOccurrences(Long productId, List<SaleItem> saleItems) {
        Map<Long, Long> scores = new HashMap<>();

        for (SaleItem item : saleItems) {
            if (!item.getProduct().getId().equals(productId)) {
                continue;
            }

            Sale sale = item.getSale();

            for (SaleItem other : sale.getItems()) {
                Long otherProductId = other.getProduct().getId();

                if (!otherProductId.equals(productId)) {
                    scores.merge(otherProductId, 1L, Long::sum);
                }
            }
        }

        return scores;
    }

    private double computeSalesDistance(
            Long candidateProductId,
            Map<Long, Long> coOccurrences,
            long maxCoOccurrence
    ) {
        if (maxCoOccurrence == 0) {
            return 1.0;
        }

        long candidateCoOccurrence = coOccurrences.getOrDefault(candidateProductId, 0L);
        double salesSimilarity = (double) candidateCoOccurrence / maxCoOccurrence;

        return 1.0 - salesSimilarity;
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9àâäéèêëïîôöùûüç\\s]", " ")
                .split("\\s+");

        Set<String> tokens = new HashSet<>();

        for (String word : words) {
            if (word.length() <= 2) {
                continue;
            }

            if (STOP_WORDS.contains(word)) {
                continue;
            }

            tokens.add(word);
        }

        return tokens;
    }

    private String buildReason(
            double categoryDistance,
            double textDistance,
            double priceDistance,
            double salesDistance
    ) {
        if (salesDistance == 0.0) {
            return "Produit souvent acheté avec le produit consulté";
        }

        if (categoryDistance == 0.0 && textDistance <= 0.70) {
            return "Produit proche selon le KNN : même catégorie et caractéristiques similaires";
        }

        if (categoryDistance == 0.0) {
            return "Produit proche selon le KNN : même catégorie";
        }

        if (priceDistance <= 0.30) {
            return "Produit proche selon le KNN : gamme de prix similaire";
        }

        return "Produit sélectionné parmi les plus proches voisins";
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }
}