package com.example.commerceplus.domain.order.repository;

import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.payment.entity.Payment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderBulkRepository {

    private final EntityManager entityManager;

    public void saveAll(
            List<Order> orders,
            List<Payment> payments
    ) {
        for (Order order : orders) {
            entityManager.persist(order);
        }

        entityManager.flush();
        entityManager.clear();

        for (Payment payment : payments) {
            entityManager.persist(payment);
        }

        entityManager.flush();
        entityManager.clear();
    }
}
