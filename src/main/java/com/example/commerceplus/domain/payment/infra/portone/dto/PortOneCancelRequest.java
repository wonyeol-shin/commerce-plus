package com.example.commerceplus.domain.payment.infra.portone.dto;

// portOne 에서 주눈 응답을 받기 위함
public record PortOneCancelRequest(
        String reason,
        String storeId
) {
}
