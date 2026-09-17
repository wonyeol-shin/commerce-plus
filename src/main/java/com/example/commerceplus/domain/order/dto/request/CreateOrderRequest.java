package com.example.commerceplus.domain.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "장바구니 아이템 ID는 최소 1개 이상이어야 합니다.")
        List<@NotNull Long> cartItemIds) {
}
