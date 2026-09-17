package com.example.commerceplus.domain.product.dto.request;

import com.example.commerceplus.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PatchProductRequest(
        @NotNull @NotBlank
        String name,
        @Min(1)
        int price,
        @NotNull @NotBlank
        String comment,
        @NotNull
        ProductCategory category
) {
}
