package com.example.commerceplus.domain.order.entity;

import com.example.commerceplus.common.entity.BaseTimeEntity;
import com.example.commerceplus.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false,length = 200)
    private String productName;

    @Column(name = "price_snapshot", nullable = false, columnDefinition = "int UNSIGNED")
    private int priceSnapshot;

    @Column(nullable = false, columnDefinition = "int UNSIGNED")
    private int quantity;

    // 주문 항목을 생성하면서 상품 정보를 스냅샷으로 저장
    private OrderItem(Product product, int priceSnapshot, int quantity) {
        this.product = product;
        this.productName = product.getName();
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
    }

    public static OrderItem create(Product product, int priceSnapshot, int quantity) {
        return new OrderItem(product, priceSnapshot, quantity);
    }

    // 주문과 양방향 관계 설정
    void setOrder(Order order) {
        this.order = order;
    }

    // 이 주문 항목의 소계 금액 계산
    public int getSubtotal() {
        return priceSnapshot * quantity;
    }
}
