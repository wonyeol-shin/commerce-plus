package com.example.commerceplus.domain.order.repository;

import com.example.commerceplus.domain.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    // 목록 조회 (페이징 O, orderItems는 LAZY)
    // ORDER BY는 JPQL에서 처리
    // Pageable은 순수하게 페이징만 담당
    @Query("SELECT o FROM Order o WHERE o.member.id = :memberId ORDER BY o.createdAt DESC")
    Page<Order> findByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    // 상세 조회 (fetch join으로 orderItems 함께 로드)
    @Query("""
    SELECT DISTINCT o
    FROM Order o
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH oi.product
    WHERE o.id = :orderId
    """)
    Optional<Order> findByIdWithOrderItems(@Param("orderId") Long orderId);

    //동일 주문의 상태 변경을 한 번에 하나씩 진행
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :orderId")
    Optional<Order> findByIdWithLock(@Param("orderId") Long orderId);

    // 주문생성 후 30분이 지난 주문을 찾음
    @Query("SELECT o FROM Order o WHERE o.createdAt < :thresholdTime AND o.status = PAYMENT_PENDING")
    List<Order> findExpireOrders(@Param("thresholdTime") LocalDateTime thresholdTime);
}
