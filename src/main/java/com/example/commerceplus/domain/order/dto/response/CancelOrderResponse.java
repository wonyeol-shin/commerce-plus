package com.example.commerceplus.domain.order.dto.response;

import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderStatus;
import com.example.commerceplus.domain.payment.entity.PaymentStatus;

public record CancelOrderResponse(
        Long id,
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus payStatus
) {


    public CancelOrderResponse(Order order, PaymentStatus paymentStatus) {
        this(order.getId(), order.getOrderNumber(), order.getStatus(), paymentStatus);
    }

    public static CancelOrderResponse from(
            Order order,
            PaymentStatus paymentStatus
    ) {
        return new CancelOrderResponse(order, paymentStatus);
    }
}
