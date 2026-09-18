package com.example.commerceplus.domain.order.service;

import com.example.commerceplus.domain.cart.entity.CartItem;
import com.example.commerceplus.domain.order.entity.OrderItem;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderLockProcessor {

    private final ProductService productService;

    public List<OrderItem> lockAndCreateOrderItems(List<CartItem> cartItems) {
        // 데드락 방지 정렬
        List<CartItem> sortedCartItems = cartItems.stream()
                .sorted(Comparator.comparing(CartItem::getProductId))
                .toList();

        List<OrderItem> orderItems = new ArrayList<>();
        // 반복문 돌며 락 조회 및 재고 차감, 장바구니 상품 생성
        for (CartItem cartItem : sortedCartItems) {
            Product product = productService.findProductByIdWithLock(cartItem.getProductId());
            product.decreaseStock(cartItem.getQuantity());

            OrderItem orderItem = OrderItem.create(product, product.getPrice(), cartItem.getQuantity());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    public void lockAndRestoreOrderItems(List<OrderItem> orderItems) {
        // 데드락 방지 정렬
        List<OrderItem> sortedOrderItems = orderItems.stream()
                .sorted(Comparator.comparing(OrderItem::getProductId))
                .toList();

        for (OrderItem orderItem : sortedOrderItems) {
            Product product = productService.findProductByIdWithLock(orderItem.getProductId());
            product.restoreStock(orderItem.getQuantity());
        }
    }
}
