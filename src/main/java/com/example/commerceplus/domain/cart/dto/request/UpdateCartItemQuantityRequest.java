package com.example.commerceplus.domain.cart.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Min(value = 1, message = "수량은 1개 이상") int quantity
){}
