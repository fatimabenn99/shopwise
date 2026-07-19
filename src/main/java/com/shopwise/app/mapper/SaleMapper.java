package com.shopwise.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.shopwise.app.dto.response.SaleItemResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "saleDate", source = "saleDate")
    SaleResponse toResponse(Sale sale);

    List<SaleResponse> toResponseList(List<Sale> sales);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    SaleItemResponse toItemResponse(SaleItem item);

    List<SaleItemResponse> toItemResponseList(List<SaleItem> items);
}