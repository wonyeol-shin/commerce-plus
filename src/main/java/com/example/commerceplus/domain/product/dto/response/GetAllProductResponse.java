package com.example.commerceplus.domain.product.dto.response;

import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionResponse;

public record GetAllProductResponse(
        Long id,
        String name,
        int price,
        String category,
        String categoryDescription
)
{
    public static GetAllProductResponse from(SearchProductConditionResponse product) {
        return new GetAllProductResponse(
                product.id(),
                product.name(),
                product.price(),
                product.category().name(),
                product.category().getDescription()
        );
    }
}
