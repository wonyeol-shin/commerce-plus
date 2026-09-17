package com.example.commerceplus.domain.payment.domain;

// portOne 응답전체를 사용하지 않고 일부만 사용하려는 공통 응답 형식
public record PaymentGatewayResponse(String id, String status, long totalAmount) {
}
