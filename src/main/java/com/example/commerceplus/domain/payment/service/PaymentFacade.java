package com.example.commerceplus.domain.payment.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.payment.domain.PaymentGateway;
import com.example.commerceplus.domain.payment.domain.PaymentGatewayResponse;
import com.example.commerceplus.domain.payment.dto.request.PostPaymentMockRequest;
import com.example.commerceplus.domain.payment.dto.request.PostPaymentRequest;
import com.example.commerceplus.domain.payment.dto.response.PaymentResponse;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// 결제 순서 조율만 담딩
public class PaymentFacade {
    private final PaymentService  paymentService;
    private final PaymentExecutionService paymentExecutionService;
    private final PaymentGateway paymentGateway;

    // portOne 결제
    public PaymentResponse confirm(Long memberId, PostPaymentRequest request) {
        Payment payment = paymentService.findByOrderIdWithOrder(request.orderId());
        Order order = payment.getOrder();
        // 조회한 주문이 로그인한 회원의 주문인지 확인 소유자 검사
        order.validateOwner(memberId);
        // 결제 주문, 상태와 portOneId 검증
        validatePayment(request, order, payment);

        PaymentGatewayResponse response = null;
        try {
            // portOne에 결제 확인 요청
            response = paymentGateway.getPayment(request.portonePaymentId());

            if (response == null) {
                log.error("포트원 응답을 받지 못함");
                throw new BusinessException(ErrorCode.INTERNAL_ERROR);
            }

            // 받은 결제 정보 검증
            validatePortonePayment(request,response,payment);
            // 완료처리
            return paymentExecutionService.completePayment(memberId,payment.getId(),order.getId());
        } // 결제 정보 검증이나 완료 처리 실패
        catch (Exception originalException){
            Payment latestPayment = paymentService.findByOrderIdWithOrder(request.orderId());
            // 다른 트랜잭션에서 어떤 형태이든 처리가 된 상황
            if (latestPayment.getStatus() != PaymentStatus.PAYMENT_PENDING ){
                return PaymentResponse.from(latestPayment);
            }

            boolean isPaid = response != null && "PAID".equals(response.status());

            if (isPaid){
                try {
                    paymentGateway.cancelPayment(request.portonePaymentId(), "결제 승인 처리 중 에러 발생 - 자동 환불");
                    paymentExecutionService.failPayment(memberId,payment.getId(),order.getId());
                }catch (Exception cancelException){
                    // 취소를 하는데 문제가 발생함
                    log.error("포트원 자동 환불 API 호출 실패! 별도 처리 필요. memberId={} paymentId={}",memberId ,payment.getId(), cancelException);
                    paymentExecutionService.markCancelFailed(payment.getId());
                }
            }else {
                // 돈이 지불되지 않은 경우(통신 등 여러 이유로)
                paymentExecutionService.failPayment(memberId,payment.getId(),order.getId());
            }

            throw originalException;
        }
    }

    // 모의 결제
    public PaymentResponse confirmMock(Long memberId, PostPaymentMockRequest request) {
        Payment payment = paymentService.findByOrderIdWithOrder(request.orderId());
        Order order = payment.getOrder();
        // 조회한 주문이 로그인한 회원의 주문인지 확인 소유자 검사
        order.validateOwner(memberId);
        // 결제 주문, 상태와 결제 금액 검증
        validatePaymentMock(request,order,payment);
        // 모의결제로 클라이언트가 보내준 결과에 따라 성공 및 실패처
        switch (request.result()) {
            //변경된 값을 응답 객체로 리턴
            case SUCCESS -> {
                return paymentExecutionService.completePayment(memberId, payment.getId(), order.getId());
            }
            case FAILED
                    -> {
                return paymentExecutionService.failPayment(memberId, payment.getId(), order.getId());
            }
        }
        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPayment(Long memberId,Long paymentId) {
        // 결제 ID로 결제와 연결된 주문을 함께 조회
        Payment payment =  paymentService.findByIdWithOrder(paymentId);
        // 결제 조회에서도 반드시 소유자를 검사
        payment.getOrder().validateOwner(memberId);
        return PaymentResponse.from(payment);
    }

    public Page<PaymentResponse> getPayments(Long memberId, Pageable pageable) {
        //로그인 회원의 결제 페이지 조회
        Page<Payment> payments = paymentService.findPaymentsByMemberId(memberId, pageable);
        return payments.map(PaymentResponse::from);
    }

    private void validatePaymentMock(PostPaymentMockRequest request, Order order, Payment payment) {
        //결제 상태 검사
        payment.validatePendingPayment();
        //주문 상태 검사
        order.validatePaymentPending();
        //금액 검사
        payment.validateAmount(request.amount());
    }

    // mock과 달리 금액 검증은 portOne에서 주는 정보로 검증
    private void validatePayment(PostPaymentRequest request, Order order, Payment payment) {
        payment.validatePendingPayment();
        order.validatePaymentPending();
        // portoneId 검사
        payment.validatePortonePaymentId(request.portonePaymentId());
    }

    private void validatePortonePayment
            (PostPaymentRequest request, PaymentGatewayResponse response, Payment payment)
    {
        // portoneId 검사
        if (!response.id().equals(request.portonePaymentId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        // 결제가 되었는지 검사
        if (!response.status().equals("PAID")){
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
        // 실제 결제 금액이 일치한지 확인
        payment.validateAmount(response.totalAmount());
    }

}
