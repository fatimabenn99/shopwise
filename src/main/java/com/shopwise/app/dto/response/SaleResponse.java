package com.shopwise.app.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleResponse {

    private Long id;
    private LocalDateTime saleDate;
    private BigDecimal total;
    private List<SaleItemResponse> items;

    public SaleResponse() {
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<SaleItemResponse> getItems() {
        return items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setItems(List<SaleItemResponse> items) {
        this.items = items;
    }
}