package com.example.commerceplus.domain.order.dto.response;

import com.example.commerceplus.domain.order.entity.OrderItem;

public record GetOrderItemResponse(
        String productName,
        int price,
        int quantity,
        int subtotal
) {
    public static GetOrderItemResponse from(OrderItem orderItem) {
        return new GetOrderItemResponse(
                orderItem.getProductName(),
                orderItem.getPriceSnapshot(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}
