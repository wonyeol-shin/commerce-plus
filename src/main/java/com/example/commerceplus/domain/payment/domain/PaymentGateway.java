package com.example.commerceplus.domain.payment.domain;

// portOne PG에만 의존하지 않기 위한 인터페이스
public interface PaymentGateway {
    // 결제 상태와 금액 조회
    PaymentGatewayResponse getPayment(String paymentId);
    // 승인된 결제 취소
    void cancelPayment(String paymentId, String reason);
}
