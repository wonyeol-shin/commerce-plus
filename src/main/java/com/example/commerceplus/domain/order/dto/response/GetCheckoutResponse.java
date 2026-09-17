package com.example.commerceplus.domain.order.dto.response;

import com.example.commerceplus.domain.cart.entity.CartItem;

import java.util.List;

public record GetCheckoutResponse(List<CheckoutItem> items, int totalPrice) {

    public static GetCheckoutResponse from(List<CheckoutItem> items, int totalPrice) {
        return new GetCheckoutResponse(items, totalPrice);
    }

    public record CheckoutItem(
            Long productId,
            String productName,
            int price,
            int quantity,
            int subtotal
    ) {
        public static CheckoutItem from(CartItem cartItem) {
            int price = cartItem.getProduct().getPrice();
            int quantity = cartItem.getQuantity();
            return new CheckoutItem(
                    cartItem.getProductId(),
                    cartItem.getProduct().getName(),
                    price,
                    quantity,
                    price * quantity
            );
        }
    }
}

