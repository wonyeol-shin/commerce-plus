package com.example.commerceplus.domain.product.dto.response;

import com.example.commerceplus.domain.product.entity.Product;

import java.time.LocalDateTime;

public record GetProductResponse(
        Long id,
        String name,
        int price,
        int stock,
        String comment,
        String category,
        String categoryDescription,
        LocalDateTime modifiedAt
)
{
    public static GetProductResponse from(Product product) {
        return new GetProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getComment(),
                product.getCategory().name(),
                product.getCategory().getDescription(),
                product.getModifiedAt()
        );
    }
}
