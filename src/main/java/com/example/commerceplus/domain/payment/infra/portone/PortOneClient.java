package com.example.commerceplus.domain.payment.infra.portone;

import com.example.commerceplus.domain.payment.domain.PaymentGateway;
import com.example.commerceplus.domain.payment.domain.PaymentGatewayResponse;
import com.example.commerceplus.domain.payment.infra.portone.dto.PortOneCancelRequest;
import com.example.commerceplus.domain.payment.infra.portone.dto.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 결제확인요청과 취소요청을 portOne 측에 함
@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {

    private final RestClient restClient;
    private final PortOneProperties portOneProperties;

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        log.info("PortOne 결제 조회: {}", paymentId);

        // portOne 응답을 PortOnePaymentResponse로 받고 PaymentGatewayResponse로 리턴
        PortOnePaymentResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/{paymentId}")
                        .queryParam("storeId", portOneProperties.getStoreId())
                        .build(paymentId))
                .retrieve()
                .body(PortOnePaymentResponse.class);

        return new PaymentGatewayResponse(
                response.id(), response.status(), response.amount().total());
    }

    @Override
    public void cancelPayment(String paymentId, String reason) {
        log.info("PortOne 결제 취소 요청: paymentId={}, reason={}", paymentId, reason);

        restClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .body(new PortOneCancelRequest(reason, portOneProperties.getStoreId()))
                .retrieve()
                .toBodilessEntity();
    }
}
