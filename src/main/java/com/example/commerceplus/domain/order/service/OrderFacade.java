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
import com.example.commerceplus.domain.order.dto.response.CancelOrderResponse;
import com.example.commerceplus.domain.order.dto.response.CreateOrderResponse;
import com.example.commerceplus.domain.order.dto.response.GetCheckoutResponse;
import com.example.commerceplus.domain.order.dto.response.GetOrderResponse;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.service.PaymentService;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final ProductService productService;
    private final CartItemService cartItemService;

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

    public CreateOrderResponse createOrder(Long memberId, CreateOrderRequest request) {

        Member member = memberService.findMemberById(memberId);
        Cart cart = cartService.findCart(member.getId()).orElseThrow( () -> new BusinessException(ErrorCode.CART_NOT_FOUND));
        List<CartItem> cartItems = cartItemService.findAndValidateCartItems(cart, request.cartItemIds());
        // 데드락 방지를 위해 ProductId로 정렬
        List<CartItem> sortedCartItems = cartItems.stream()
                .sorted(Comparator.comparing(cartItem -> cartItem.getProduct().getId()))
                .toList();
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = productService.findProductById(cartItem.getProductId());
            product.decreaseStock(cartItem.getQuantity());
            orderItems.add(new OrderItem(product, product.getPrice(), cartItem.getQuantity()));
        }

        int totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getSubtotal)
                .sum();
        Order order = orderService.createOrder(member, orderItems, totalPrice);
        Payment payment = paymentService.createPayment(order);

        // 결제 성공 시점까지 장바구니는 유지한다.
        return CreateOrderResponse.from(order, payment);
    }

    // 내 주문 목록 조회
    @Transactional(readOnly = true)
    public Page<GetOrderResponse> getOrdersAll(Long memberId, Pageable pageable) {
       return orderService.findOrdersByMemberId(memberId, pageable)
                .map(order -> {
                    // PaymentService에서 주문 ID로 Payment 객체 조회하기
                  Optional<Payment> payment = paymentService.findPaymentByOrderId(order.getId());
                  Long paymentId = payment.map(Payment::getId).orElse(null);
                    return GetOrderResponse.from(order,paymentId);
                });
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public GetOrderResponse getOrderOne(Long memberId, Long orderId) {
        Order order = orderService.findOrderById(orderId);
        order.validateOwner(memberId);
        Payment payment = paymentService.findPaymentByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return GetOrderResponse.from(order, payment.getId());
    }

    // 주문 취소
    public CancelOrderResponse cancelOrder(Long memberId, Long orderId) {
        Order order = orderService.findOrderById(orderId);
        order.validateOwner(memberId);
        Payment payment = paymentService.findPaymentByOrderId(orderId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
                );

        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.restoreStock(orderItem.getQuantity());
        }

        payment.cancel();
        order.cancel();
        return new CancelOrderResponse(
                order,
                payment.getStatus()
        );
    }
}
