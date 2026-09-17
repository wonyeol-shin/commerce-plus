package com.example.commerceplus.domain.product.dto.condition;

import com.example.commerceplus.domain.product.entity.ProductCategory;
import jakarta.validation.constraints.Min;

public record SearchProductConditionRequest(
        @Min(0)
        Integer page,
        @Min(1)
        Integer size,
        @Min(1)
        Integer minPrice,
        @Min(1)
        Integer maxPrice,
        ProductCategory category
)
{
    // 최소 가격보다 커야하고 최대 가격보다는 작아야 하는데 최소 가격이 최대 가격보다 클 경우
    public boolean isMinPriceGreaterThanMaxPrice() {
        if (minPrice == null || maxPrice == null) {return false;}
        return minPrice > maxPrice;
    }

    public SearchProductConditionRequest {
        if (page == null) page = 0;
        if (size == null) size = 9;
    }

    // 캐시에 저장할 키를 리턴
    public String getCacheKey(){
        return this.page + ":" +
                this.size + ":" +
                this.minPrice + ":" +
                this.maxPrice + ":" +
                ( (this.category != null)
                        ? this.category.name()
                        : "ALL");
    }

}
