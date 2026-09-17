package com.example.commerceplus.domain.order.entity;

import com.example.commerceplus.common.entity.BaseTimeEntity;
import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "total_price", nullable = false, columnDefinition = "int UNSIGNED")
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String orderNumber;

    // 주문 객체 초기화 하면서 주문과 상품 항목들 연결
    private Order(Member member, int totalPrice, List<OrderItem> orderItems, String orderNumber) {
        this.member = member;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PAYMENT_PENDING;
        this.orderNumber = orderNumber;
        orderItems.forEach(this::addOrderItem);
    }

    public static Order create(
            Member member,
            int totalPrice,
            List<OrderItem> orderItems,
            String orderNumber
    ) {
        return new Order(member, totalPrice, orderItems, orderNumber);
    }

    // 주문한 회원의 ID만 빠르게 조회
    public Long getMemberId() {
        return member.getId();
    }

    // 주문에 상품을 추가하면서 양방향 관계 맺기
    private void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문을 사람이 읽을 수 있는 이름으로 표현
    public String getOrderName() {
        if (orderItems.isEmpty()) {
            return "주문";
        }
        String firstName = orderItems.getFirst().getProductName();
        if (orderItems.size() == 1) {
            return firstName;
        }
        return firstName + " 외 " + (orderItems.size() - 1) + "건";
    }

    // 취소 가능한 상태 검증(결제 전 취소)
    public void cancel() {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        this.status = OrderStatus.CANCELED;
    }

    public void validateOwner(Long memberId) {
        if (!this.member.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    public void completePayment() {
        if (!this.status.canTransitTo(OrderStatus.COMPLETED)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        this.status = OrderStatus.COMPLETED;
    }

    public void validatePaymentPending() {
        //주문이 결제를 진행할 수 있는 상황인지
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

    }
}

