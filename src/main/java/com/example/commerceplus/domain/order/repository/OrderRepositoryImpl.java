package com.example.commerceplus.domain.order.repository;

import com.example.commerceplus.domain.order.dto.response.GetOrderItemResponse;
import com.example.commerceplus.domain.order.dto.response.GetOrderResponse;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.QOrder;
import com.example.commerceplus.domain.order.entity.QOrderItem;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.entity.QPayment;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetOrderResponse> findOrdersByMemberId(
            Long memberId,
            Pageable pageable
    ) {

        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QPayment payment = QPayment.payment;

        // 1. 먼저 주문 ID를 페이징해서 가져온다.
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(order.member.id.eq(memberId))
                .orderBy(order.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 주문이 하나도 없으면 바로 빈 페이지 반환
        if (orderIds.isEmpty()) {
            return new PageImpl<>(
                    List.of(),
                    pageable,
                    0
            );
        }

        // 2. 주문 + 주문상품 + 결제를 한 번에 조회한다.
        List<Tuple> results = queryFactory
                .select(order, orderItem, payment)
                .from(order)
                .join(order.orderItems, orderItem)
                .leftJoin(payment)
                .on(payment.order.eq(order))
                .where(order.id.in(orderIds))
                .fetch();

        // 3. 주문 ID 기준으로 그룹화한다.
        Map<Long, List<Tuple>> grouped = results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(order.id)
                ));

        // 4. 주문별로 GetOrderResponse를 만든다.
        List<GetOrderResponse> content = orderIds.stream()
                .map(orderId -> {

                    List<Tuple> tuples = grouped.get(orderId);

                    Tuple first = tuples.get(0);

                    Order orderEntity = first.get(order);
                    Payment paymentEntity = first.get(payment);

                    List<GetOrderItemResponse> orderItems = tuples.stream()
                            .map(tuple ->
                                    GetOrderItemResponse.from(
                                            tuple.get(orderItem)
                                    )
                            )
                            .toList();

                    return new GetOrderResponse(
                            orderEntity.getId(),
                            orderEntity.getOrderNumber(),

                            paymentEntity != null
                                    ? paymentEntity.getId()
                                    : null,

                            paymentEntity != null
                                    ? paymentEntity.getStatus().name()
                                    : null,

                            orderEntity.getStatus().name(),
                            orderEntity.getTotalPrice(),
                            orderEntity.getOrderName(),
                            orderEntity.getCreatedAt(),
                            orderItems
                    );
                })
                .toList();

        // 5. 전체 주문 개수
        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(order.member.id.eq(memberId))
                .fetchOne();

        return new PageImpl<>(
                content,
                pageable,
                total != null ? total : 0
        );
    }
}

