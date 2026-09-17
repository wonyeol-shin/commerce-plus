package com.example.commerceplus.domain.cart.dto.response;

import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.entity.CartItem;

import java.util.List;

public record CartResponse (
        Long id,
        // 장바구니에 담긴 상품 리스트
        List<CartItemResponse> cartItems
                             ) {
    public static CartResponse from(Cart cart , List<CartItem> cartItems) {
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(CartItemResponse::from)
                .toList();

        // 3. CartResponse 생성 후 반환
        return new CartResponse(cart.getId(),itemResponses);
    }
}
