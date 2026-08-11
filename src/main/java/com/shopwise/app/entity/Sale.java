package com.shopwise.app.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "SALES")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
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

    public List<SaleItem> getItems() {
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

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }
    
    public void clearItems() {
        for (SaleItem item : items) {
            item.setSale(null);
        }

        items.clear();
    }
}