package com.example.commerceplus.domain.order.repository;

import com.example.commerceplus.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 목록 조회 (페이징 O, orderItems는 LAZY)
    // ORDER BY는 JPQL에서 처리
    // Pageable은 순수하게 페이징만 담당
    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId ORDER BY o.createdAt DESC")
    Page<Order> findByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    // 상세 조회 (fetch join으로 orderItems 함께 로드)
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);

    // 주문 취소
    Optional<Order> findByIdAndMemberId(
            Long orderId,
            Long memberId
    );

    // 주문생성 후 30분이 지난 주문을 찾음
    @Query("SELECT o FROM Order o WHERE o.createdAt < :thresholdTime AND o.status = PAYMENT_PENDING")
    List<Order> findExpireOrders(@Param("thresholdTime") LocalDateTime thresholdTime);
}
