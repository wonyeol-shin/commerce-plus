package com.example.commerceplus.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostPaymentRequest(
        @NotNull @Min(1)
        Long orderId,
        @NotBlank
        String portonePaymentId
) {
}
