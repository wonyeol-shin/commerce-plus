package com.example.commerceplus.domain.order.dto.request;

import jakarta.validation.constraints.Min;

public record SearchOrderConditionRequest(
        @Min(0)
        Integer page,
        @Min(1)
        Integer size
) {
    public SearchOrderConditionRequest {
        if (page == null) page = 0;
        if (size == null) size = 9;
    }
}
