package com.example.commerceplus.domain.payment.dto.request;

import jakarta.validation.constraints.Min;

public record SearchPaymentConditionRequest (
        @Min(0)
        Integer page,
        @Min(1)
        Integer size
) {
    public SearchPaymentConditionRequest {
        if (page == null) page = 0;
        if (size == null) size = 9;
    }
}

