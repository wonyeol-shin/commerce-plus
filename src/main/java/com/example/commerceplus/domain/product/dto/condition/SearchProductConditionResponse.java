package com.example.commerceplus.domain.product.dto.condition;

import com.example.commerceplus.domain.product.entity.ProductCategory;

// 캐시를 위해 queryDsl에서 stock을 제외한 pk, name, price, category 만 조회 받기 위한 dto
public record SearchProductConditionResponse(
    Long id,
    String name,
    int price,
    ProductCategory category
) {

}
