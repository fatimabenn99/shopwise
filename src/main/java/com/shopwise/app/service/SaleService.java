package com.shopwise.app.service;

import java.util.List;

import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;

public interface SaleService {

    SaleResponse create(CreateSaleRequest request);

    List<SaleResponse> getAll();

    SaleResponse getById(Long id);
}