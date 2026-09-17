package com.example.commerceplus.domain.payment.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.cart.service.CartItemService;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.order.service.OrderService;
import com.example.commerceplus.domain.payment.dto.response.PaymentResponse;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
// 결제 시 반드시 모두성공 혹은 모두 실패만 해야하는 기능을 담당
public class PaymentExecutionService {

    private final ProductService productService;
    private final CartItemService cartItemService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    @Transactional
    public PaymentResponse completePayment(Long memberId, Long paymentId, Long orderId) {
        // 일반 Facade가 아니라 여기서 락을 걸음
        Payment payment = paymentService.findPaymentByIdWithLock(paymentId);
        Order order = orderService.findOderIdWithLock(orderId);

        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        // portOne과 같은 검증 후 시간이 지남에따라 데이터가 변동되었을 수 있어 한번 더 검증을 함
        order.validateOwner(memberId);
        order.validatePaymentPending();
        payment.validatePendingPayment();

        // 이번 주문에 포함된 상품 ID를 가져옴
        List<Long> productIds = getOrderedProductIds(order);
        // 결제를 완료하고 완료 시각을 기록
        payment.complete(LocalDateTime.now());
        // 주문 상태를 완료로 변경
        order.completePayment();
        // 해당 회원의 장바구니에서 주문한 상품만 삭제
        cartItemService.deleteOrderedProducts(memberId, productIds);
        return PaymentResponse.from(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentResponse failPayment(Long memberId, Long paymentId, Long orderId) {
        Payment payment = paymentService.findPaymentByIdWithLock(paymentId);
        Order order = orderService.findOderIdWithLock(orderId);

        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        // portOne과 같은 검증 후 시간이 지남에따라 데이터가 변동되었을 수 있어 한번 더 검증을 함
        order.validateOwner(memberId);
        payment.validatePendingPayment();
        order.validatePaymentPending();

        // 복구할 상품과 수량을 주문 기록에서 추출
        Map<Long,Integer> quantities = getRestoreQuantities(order);
        // 실패 결과를 저장할 상태로 변경
        payment.fail();
        order.cancel();
        // 복구 중 예외가 발생하면  상태 변경도 함께 롤백
        productService.restoreStocks(quantities);
        return PaymentResponse.from(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCancelFailed(Long paymentId) {
        Payment payment = paymentService.findPaymentByIdWithLock(paymentId);
        payment.cancelFailed();
    }

    private Map<Long,Integer> getRestoreQuantities(Order order) {
        Map<Long,Integer> quantities = new TreeMap<>();
        for (OrderItem item : order.getOrderItems()) {
            Long productId = item.getProduct().getId();
            int quantity = item.getQuantity();
            // 같은 상품이 여러 항목에 있으면 수량을 합침
            quantities.merge(productId, quantity, Math::addExact);
        }
        return quantities;
    }

    private List<Long> getOrderedProductIds(Order order) {
        return order.getOrderItems().stream()
                .map(item ->item.getProduct().getId())
                .distinct()
                .toList();
    }

}
