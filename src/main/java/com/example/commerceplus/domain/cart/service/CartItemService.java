package com.example.commerceplus.domain.cart.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.cart.dto.response.CartResponse;
import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.entity.CartItem;
import com.example.commerceplus.domain.cart.repository.CartItemRepository;
import com.example.commerceplus.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public int addItem(Cart cart, Product product, int quantity) {
        // Cart와 Product조합의 CartItem 이 있는지 확인
        Optional<CartItem> foundCartItem = cartItemRepository.findByCartAndProduct(cart, product);
        // 현재 장바구니 수량과 담으려는 수량을 더함
        int expectedQuantity = foundCartItem.map(CartItem::getQuantity).orElse(0) + quantity;

        if (!product.isEnoughStock(expectedQuantity)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 신규 생성인 경우
        if (foundCartItem.isEmpty()) {
            CartItem newCartItem = CartItem.createCartItem(cart, product, quantity);
            cartItemRepository.save(newCartItem);
            return newCartItem.getQuantity();
        }

        // 이미 존재하는 경우
        CartItem existingCartItem = foundCartItem.get();
        existingCartItem.addQuantity(quantity);
        return existingCartItem.getQuantity();
    }

    //장바구에 담긴 상품 조회
   @Transactional(readOnly = true)
   public CartResponse getCartItems(Cart cart) {
         List<CartItem> cartItems = cartItemRepository.findByCart(cart);
         return CartResponse.from(cart, cartItems);
}

    @Transactional(readOnly = true )//장바구니에 상품이 몇개 담겼는지
    public int getExistingQuantity(Cart cart, Product product) {
        Integer countedQuantity = cartItemRepository.sumQuantityByCartAndProduct(cart, product);
        return (countedQuantity != null)? countedQuantity : 0;
    }

    public int UpdateQuantity(Cart cart, Long cartItemId, int quantity) {
        //타인의 아이템 수정 방지
        CartItem cartItem = cartItemRepository.findByIdAndCart(cartItemId,cart)
                .orElseThrow(()->new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        //수량 변경
        cartItem.changeQuantity(quantity);
        return cartItem.getQuantity();
    }

    public void deleteCatItem(Long memberId, Long cartItemId) {
        //db 벌크 삭제
        int deleteCount = cartItemRepository.deleteByIdAndMemberId(cartItemId, memberId);

        if (deleteCount == 0) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    public void deleteAllCartItem(Long memberId) {
        //회원 아이디를 기반으 해당으로 회의 모든 장바구니 아이템을 벌크 삭제
        cartItemRepository.deleteAllByMemberId(memberId);
    }

    public List<CartItem> findAndValidateCartItems(Cart cart, List<Long> cartItemIds) {
        List<CartItem> findCartItems = cartItemRepository.findByCartAndIds(cart, cartItemIds);

        if (findCartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        if (cartItemIds.size() != findCartItems.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        return findCartItems;
    }


    public void deleteOrderedProducts(Long memberId, List<Long> productIds) {
        // 삭제 대상이 없으면 쿼리를 실행하지 않습니다.
        if (productIds.isEmpty()) {
            return;
        }

        // 회원 ID와 상품 ID 목록을 Repository에 전달합니다.
        cartItemRepository.deleteByMemberIdAndProductIds(memberId, productIds);
    }
}