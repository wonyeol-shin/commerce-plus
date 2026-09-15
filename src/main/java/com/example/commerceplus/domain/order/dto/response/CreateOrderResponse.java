package com.example.commerceplus.domain.order.dto.response;

import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.payment.entity.Payment;

public record CreateOrderResponse(
        Long orderId,
        String orderNumber,
        String portonePaymentId,
        Long paymentId,
        int totalPrice
) {
    public static CreateOrderResponse from(Order order, Payment payment) {
        return new CreateOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                payment.getPortonePaymentId(),
                payment.getId(),
                order.getTotalPrice()
        );
    }
}