package com.example.commerceplus.domain.cart.dto.response;

import com.example.commerceplus.domain.cart.entity.CartItem;

public record CartItemResponse (
        Long cartItemId,
        Long productId,
        String productName,
        int price,
        int quantity
) {
 public static CartItemResponse from(CartItem cartItem) {

     return new CartItemResponse(
             cartItem.getId(),
             cartItem.getProduct().getId(),
             cartItem.getProduct().getName(),
             cartItem.getProduct().getPrice(),
             cartItem.getQuantity()
     );
 }
}
