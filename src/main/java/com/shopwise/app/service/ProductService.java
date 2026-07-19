package com.shopwise.app.service;

import java.util.List;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse getById(Long id);

    List<ProductResponse> getAll();

    ProductResponse update(Long id, UpdateProductRequest request);

    void delete(Long id);
}