package com.example.commerceplus.domain.payment.entity;

import com.example.commerceplus.common.entity.BaseTimeEntity;
import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "createdAt" , column =@Column(nullable = false, updatable = false))
//결제 금액 검증 , 상태 변경
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 1건당 결제 1건
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    // 결제 소유 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 결제 금액은 서버에 저장된 주문 총액을 사용
    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int amount;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "portone_payment_id", unique = true, nullable = false, length = 100)
    private String portonePaymentId;

    // 결제 완료 시각
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // 외부에서 임의로 Payment를 생성하지 못하도록 private 생성자 사용
    private Payment(Order order, String portonePaymentId) {
        this.order = order;

        // 결제 회원은 주문 회원과 동일하게 설정
        this.member = order.getMember();

        // 클라이언트가 보내는 금액이 아니라 서버의 주문 총액을 사용
        this.amount = order.getTotalPrice();

        // 최초 결제 상태는 항상 결제 대기
        this.status = PaymentStatus.PAYMENT_PENDING;

        this.portonePaymentId = portonePaymentId;
    }

    // 주문 생성 시 결제 대기 데이터를 생성
    public static Payment create(Order order, String portonePaymentId) {
        return new Payment(order, portonePaymentId);
    }

    public void validatePendingPayment() {
        if (this.status != PaymentStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }
    }

    // 요청 금액과 서버에 저장된 결제 금액이 일치하는지 검증
    public void validateAmount(long requestAmount) {
        if (this.amount != requestAmount) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    // 요청한 portoneId와 저장된 portoneId가 일치하는지 검증
    public void validatePortonePaymentId(String portonePaymentId) {
        if (!this.portonePaymentId.equals(portonePaymentId)) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    // 결제 성공: PAYMENT_PENDING → COMPLETED
    public void complete(LocalDateTime paidAt) {
        if (!this.status.canTransitTo(PaymentStatus.COMPLETED)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        this.status = PaymentStatus.COMPLETED;
        this.paidAt = paidAt;
    }

    // 결제 실패: PAYMENT_PENDING → FAILED
    public void fail() {
        if (!this.status.canTransitTo(PaymentStatus.FAILED)) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        this.status = PaymentStatus.FAILED;
    }

    // 결제 완료 후 취소: COMPLETED → CANCELED
    public void cancel() {
        if (!this.status.canTransitTo(PaymentStatus.CANCELED)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.status = PaymentStatus.CANCELED;
    }

    // 결제가 잘못되었지만 취소가 되지 않음 : PAYMENT_PENDING -> CANCEL_FAILED
    public void cancelFailed() {
        if (!this.status.canTransitTo(PaymentStatus.CANCEL_FAILED)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        this.status = PaymentStatus.CANCEL_FAILED;
    }

    // 연관된 주문 ID 반환
    public Long getOrderId() {
        return order.getId();
    }

    // 결제 소유 회원 ID 반환
    public Long getMemberId() {
        return member.getId();
    }
}