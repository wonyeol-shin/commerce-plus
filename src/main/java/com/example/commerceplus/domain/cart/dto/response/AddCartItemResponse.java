package com.example.commerceplus.domain.cart.dto.response;

public record AddCartItemResponse(
        int cartItemQuantity
){
    public static AddCartItemResponse of(int cartItemQuantity) {
        return new AddCartItemResponse(cartItemQuantity);
    }
}
