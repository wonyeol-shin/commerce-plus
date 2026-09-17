package com.example.commerceplus.domain.payment.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// portOne 에서 주눈 응답을 받기 위함 나중에 공통 응답인 PaymentGatewayResponse로 전환
public record PortOnePaymentResponse(
        String id,
        String status,
        Amount amount
)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(long total){ }
}
