package com.shopwise.app.dto.response;

import java.math.BigDecimal;

public class RecommendationResponse {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private double similarityScore;
    private String reason;

    public RecommendationResponse() {
    }

    public RecommendationResponse(Long productId, String productName, BigDecimal price, double similarityScore, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.similarityScore = similarityScore;
        this.reason = reason;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public String getReason() {
        return reason;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}