package com.shopwise.app.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class UpdateSaleRequest {

    @NotEmpty
    @Valid
    private List<CreateSaleItemRequest> items;

    public UpdateSaleRequest() {
    }

    public List<CreateSaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<CreateSaleItemRequest> items) {
        this.items = items;
    }
}