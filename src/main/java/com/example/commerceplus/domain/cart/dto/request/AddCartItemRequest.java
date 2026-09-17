package com.example.commerceplus.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest (
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1개 이상어야 합니다")
    Integer quantity){

}

