package com.shopwise.app.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

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
import com.shopwise.app.service.SaleService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;

    public SaleServiceImpl(
            SaleRepository saleRepository,
            ProductRepository productRepository,
            SaleMapper saleMapper
    ) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.saleMapper = saleMapper;
    }

    @Override
    public SaleResponse create(CreateSaleRequest request) {
        Sale sale = new Sale();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateSaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);

            sale.addItem(item);
            total = total.add(lineTotal);
        }

        sale.setTotal(total);

        Sale saved = saleRepository.save(sale);
        return saleMapper.toResponse(saved);
    }

    @Override
    public List<SaleResponse> getAll() {
        return saleMapper.toResponseList(
                saleRepository.findAllByOrderBySaleDateDesc()
        );
    }

    @Override
    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found"));

        return saleMapper.toResponse(sale);
    }
    
    @Override
    public SaleResponse update(Long id, UpdateSaleRequest request) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sale not found"));

        sale.clearItems();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateSaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);

            sale.addItem(item);
            total = total.add(lineTotal);
        }

        sale.setTotal(total);

        Sale updated = saleRepository.save(sale);
        return saleMapper.toResponse(updated);
    }
}