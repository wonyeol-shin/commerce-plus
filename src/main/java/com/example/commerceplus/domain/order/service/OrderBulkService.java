package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.repository.MemberRepository;
import com.example.commerceplus.domain.order.entity.Order;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.order.repository.OrderBulkRepository;
import com.example.commerceplus.domain.payment.entity.Payment;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class OrderBulkService {

    private final OrderBulkRepository orderBulkRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void createAndSaveBulkOrders() {

        List<Member> members = memberRepository.findAll();
        List<Product> products = productRepository.findAll();

        List<Order> orders = new ArrayList<>();
        List<Payment> payments = new ArrayList<>();

        for (int i = 1; i <= 50000; i++) {

            Member member = members.get(i % members.size());
            Product product = products.get(i % products.size());

            int quantity = 1;
            int totalPrice = product.getPrice() * quantity;

            OrderItem orderItem = OrderItem.create(product, product.getPrice(), quantity);

            Order order = Order.create(
                    member,
                    totalPrice,
                    List.of(orderItem),
                    "BULK-" + i
            );

            String orderNumber = "PAY_"  + UUID.randomUUID();

            Payment payment = Payment.create(order,orderNumber);

            orders.add(order);
            payments.add(payment);
        }

        orderBulkRepository.saveAll(orders, payments);
    }
}
