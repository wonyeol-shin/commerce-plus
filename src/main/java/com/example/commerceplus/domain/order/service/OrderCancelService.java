package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderStatus;
import com.example.commerceplus.domain.order.repository.OrderRepository;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderLockProcessor orderLockProcessor;

    // 결제를 하지않고 재고만 차지하는 주믄을 취소
    // 주문들을 취소 시 각 주문마다 별도의 트랜잭션을 주어서 오랜시간 락을 점유하는걸 방지한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelOrderAndRestoreStock(Long expiredOrderId) {

        // 취소 전 order에 lock을 걸어 동시성을 방지, 주문확정과 데드락 방지를 위해 주문 -> 결제 순서로 lock
        Order order = orderRepository.findByIdWithLock(expiredOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        // 최종적으로 한번 더 결제 대기상태인지 검증 (이 시점에 결제를 완료했을 수 있음)
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return;
        }
        order.cancel();

        // 상품 재고 차감
        orderLockProcessor.lockAndRestoreOrderItems(order.getOrderItems());

        // 주문 락 및 취소
         Payment payment = paymentRepository.findByOrderIdWithLock(expiredOrderId).orElseThrow(()
                -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
         payment.cancel();
    }
}
