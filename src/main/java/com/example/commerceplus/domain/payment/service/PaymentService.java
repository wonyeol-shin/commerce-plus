package com.example.commerceplus.domain.payment.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
//결제 생성 조회 역할
public class PaymentService {

    private final PaymentRepository paymentRepository;
  
    // 결제 생성
    @Transactional
    public Payment createPayment(Order order) {
        String orderNumber = "PAY-"  + UUID.randomUUID();
        Payment payment = Payment.create(order, orderNumber);
        return paymentRepository.save(payment);
    }

    public Payment findByOrderIdWithOrder(Long orderId) {
        return paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment findByIdWithOrder(Long paymentId) {
        return paymentRepository.findByIdWithOrder(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }
  
    public Optional<Payment> findPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
  
    public Page<Payment> findPaymentsByMemberId(Long memberId, Pageable pageable) {
       return paymentRepository.findPaymentsByMemberId(memberId, pageable);
    }

    public Payment findPaymentByIdWithLock(Long paymentId) {
        return paymentRepository.findByIdWithLock(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    // 결제 취소
    @Transactional
    public Payment cancelPayment(Long orderId) {
        Payment payment = paymentRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.cancel();
        return payment;
    }
}
