package com.shopwise.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.ConflictException;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.ProductMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.service.ProductService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse create(CreateProductRequest request) {
    	
    	if (productRepository.existsByNameIgnoreCase(request.getName())) {
    	    throw new ConflictException("Product name already exists");
    	}
    	
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Override
    public List<ProductResponse> getAll() {
        List<Product> products = productRepository.findAll();
        return productMapper.toResponseList(products);
    }

    @Override
    public ProductResponse update(Long id, UpdateProductRequest request) {
    	
    	if (productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
    	    throw new ConflictException("Product name already exists");
    	}
    	
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        productMapper.updateEntity(request, product);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }

        productRepository.deleteById(id);
    }
}