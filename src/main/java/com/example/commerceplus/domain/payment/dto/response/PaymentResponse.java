package com.example.commerceplus.domain.payment.dto.response;

import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.payment.entity.Payment;

import java.time.LocalDateTime;

public record PaymentResponse(
    Long paymentId,
    Long orderId,
    String portonePaymentId,
    int amount,
    String payStatus,
    String orderStatus,
    LocalDateTime paidAt
) {

    public static PaymentResponse from(Payment payment) {
        Order order = payment.getOrder();

        return new PaymentResponse (
            payment.getId(),
            order.getId(),
            payment.getPortonePaymentId(),
            payment.getAmount(),
            payment.getStatus().name(),
            order.getStatus().name(),
            payment.getPaidAt()
        );
    }
}