package com.example.commerceplus.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PostPaymentMockRequest(
        @NotNull @Min(1)
        Long orderId,
        @NotNull
        MockPaymentResult result,
        @NotNull @Min(1) 
        Integer amount
) {
}
