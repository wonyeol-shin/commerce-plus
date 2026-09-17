package com.example.commerceplus.domain.cart.dto.response;

public record UpdateCartItemQuantityResponse(
        int cartItemQuantity // 변경 후 최종 수량
) {
    public  static UpdateCartItemQuantityResponse of(int cartItemQuantity){
        return new UpdateCartItemQuantityResponse(cartItemQuantity);
    }
}
