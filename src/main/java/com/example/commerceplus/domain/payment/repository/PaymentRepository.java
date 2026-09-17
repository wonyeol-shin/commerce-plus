package com.example.commerceplus.domain.payment.repository;

import com.example.commerceplus.domain.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 모의 결제 처리 시 주문 ID로 결제 조회
    @Query("SELECT p FROM Payment p where p.order.id = :orderId")
    Optional<Payment> findByOrderId(@Param("orderId") Long orderId);

    // 결제 확정 - orderId 기준 조회 (Order fetch join)
    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p join FETCH p.order WHERE p.id = :paymentId")
    Optional<Payment> findByIdWithOrder(@Param("paymentId") Long paymentId);

    @EntityGraph(attributePaths = "order")
    @Query(value = "SELECT p FROM Payment p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC , p.id DESC ",
    countQuery = "SELECT COUNT(p) FROM Payment p WHERE p.member.id = :memberId")
    Page<Payment> findPaymentsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :paymentId ")
    Optional<Payment> findByIdWithLock(@Param("paymentId") Long paymentId);
}
