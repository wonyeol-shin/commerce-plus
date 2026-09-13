package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Member member, List<OrderItem> orderItems, int totalPrice) {
        String orderNumber = "ORD-" + UUID.randomUUID();
        Order order = Order.create(member, totalPrice, orderItems, orderNumber);
        return orderRepository.save(order);
    }

    public Page<Order> findOrdersByMemberId(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable);
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    public List<Order> findPendingOrdersOlderThan(LocalDateTime thresholdTime) {
       return orderRepository.findExpireOrders(thresholdTime);
    }
}