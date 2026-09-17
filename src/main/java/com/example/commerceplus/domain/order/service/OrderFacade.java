package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.entity.CartItem;
import com.example.commerceplus.domain.cart.service.CartItemService;
import com.example.commerceplus.domain.cart.service.CartService;
import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.sevice.MemberService;
import com.example.commerceplus.domain.order.dto.request.CreateOrderRequest;
import com.example.commerceplus.domain.order.dto.response.*;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.service.PaymentService;
import com.example.commerceplus.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class OrderFacade {

    private final CartService cartService;
    private final MemberService memberService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CartItemService cartItemService;
    private final OrderCalculationProcessor orderCalculationProcessor;

    @Transactional(readOnly = true)
    public GetCheckoutResponse getCheckoutOne(Long memberId, List<Long> cartItemIds) {

        Member member = memberService.findMemberById(memberId);
        Cart cart = cartService.findCart(member.getId()).orElseThrow( () -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems = cartItemService.findAndValidateCartItems(cart, cartItemIds);
        List<GetCheckoutResponse.CheckoutItem> items = cartItems.stream()
                .map(GetCheckoutResponse.CheckoutItem::from)
                .toList();

        int totalPrice = items.stream()
                .mapToInt(GetCheckoutResponse.CheckoutItem::subtotal)
                .sum();

        return GetCheckoutResponse.from(items, totalPrice);
    }

    //생성할 주문 항목을 담는 빈 목록
    public CreateOrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        // 로그인한 사용자의 cart를 가져옴
        Member member = memberService.findMemberById(memberId);
        Cart cart = cartService.findCart(member.getId()).orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        // 장바구니 상품이 유효한지 확인하고 장바구니 생성 및 상품 재고차감
        List<CartItem> cartItems = cartItemService.findAndValidateCartItems(cart, request.cartItemIds());
        List<OrderItem> orderItems = orderCalculationProcessor.lockAndCreateOrderItems(cartItems);

        // 장바구니에 담긴 총 상품 계산 후 주문 생성
        int totalPrice = orderItems.stream().mapToInt(OrderItem::getSubtotal).sum();
        Order order = orderService.createOrder(member, orderItems, totalPrice);

        //  결제 생성
        Payment payment = paymentService.createPayment(order);

        return CreateOrderResponse.from(order, payment);
    }

    // 내 주문 목록 조회
    @Transactional(readOnly = true)
    public Page<GetAllOrderResponse> getOrdersAll(Long memberId, Pageable pageable) {
       return orderService.findOrdersByMemberId(memberId, pageable)
                .map(order -> {
                    // PaymentService에서 주문 ID로 Payment 객체 조회하기
                  Optional<Payment> payment = paymentService.findPaymentByOrderId(order.getId());
                  Long paymentId = payment.map(Payment::getId).orElse(null);
                    return GetAllOrderResponse.from(order,paymentId);
                });
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public GetOrderResponse getOrderOne(Long memberId, Long orderId) {
        Order order = orderService.findOrderById(orderId);
        order.validateOwner(memberId);
        Payment payment = paymentService.findPaymentByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return GetOrderResponse.from(order, payment.getId(), payment.getStatus().name());
    }

    // 주문 취소
    public CancelOrderResponse cancelOrder(Long memberId, Long orderId) {
        // 주문취소와 재고 복구
        Order order = orderService.cancelOrder(orderId, memberId);

        Payment payment = paymentService.findPaymentByOrderId(orderId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
                );
        payment.cancel();

        return new CancelOrderResponse(
                order,
                payment.getStatus()
        );
    }
}
