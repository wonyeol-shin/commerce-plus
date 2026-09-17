package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.order.entity.OrderStatus;
import com.example.commerceplus.domain.order.repository.OrderRepository;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 결제를 하지않고 재고만 차지하는 주믄을 취소
    // 주문들을 취소 시 각 주문마다 별도의 트랜잭션을 주어서 오랜시간 락을 점유하는걸 방지한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelOrderAndRestoreStock(Long expiredOrderId) {

        // N+1방지를 위해 OrderItem도 함께 가져옴 다만, Product는 락을 걸기 위해 같이 가져오지 않음
        Order order = orderRepository.findByIdWithOrderItems(expiredOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 최종적으로 한번 더 결제 대기상태인지 검증 (이 시점에 결제를 완료했을 수 있음)
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return;
        }

        // 데드락 방지를 위해 ProductId를 기준으로 정렬
        List<OrderItem> orderItems = order.getOrderItems().stream()
                .sorted(Comparator.comparing(orderitem -> orderitem.getProduct().getId()))
                .toList();

        // 상품을 lock을 걸고 재고 복구
        for (OrderItem orderItem : orderItems) {
            Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            product.restoreStock(orderItem.getQuantity());
        }

        order.cancel();
    }
}
