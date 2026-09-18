package com.example.commerceplus.domain.cart.repository;

import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.entity.CartItem;
import com.example.commerceplus.domain.product.entity.Product;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCart(Long id, Cart cart);

    // [Cart, Order ] 장바구니에 담긴 상품들을 조회
    @Query(" SELECT c FROM CartItem c LEFT JOIN FETCH c.product p WHERE c.cart = :cart")
    List<CartItem> findByCart(@Param("cart") Cart cart);

    // 장바구니에 담긴 상품이 몇개 담겨있는지 조회
    @Query(" SELECT SUM(c.quantity)FROM CartItem c WHERE c.cart = :cart AND c.product = :product")
    @Nullable
    Integer sumQuantityByCartAndProduct(@Param("cart") Cart cart, @Param("product") Product product);

    // Cart와 Product조합의 CartItem 이 있는지 확인
    @Query(" SELECT c FROM CartItem c WHERE c.cart = :cart AND c.product = :product")
    Optional<CartItem> findByCartAndProduct(@Param("cart") Cart cart, @Param("product") Product product);

    @Modifying
    // 항목 id와 회원 id를 동시에 검사하므로 다른 회원의 장바구니 항목을 id만으로 삭제하지 못하게 함.
    @Query("DELETE FROM CartItem ci WHERE ci.id = :id AND ci.cart.member.id = :memberId")
    int deleteByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

    @Modifying
    // 해당 회원 장바구니의 모든 항목을 삭제
    @Query("DELETE FROM CartItem ci WHERE ci.cart.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT DISTINCT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart = :cart AND ci.id IN :ids")
    List<CartItem> findByCartAndIds(@Param("cart") Cart cart, @Param("ids") List<Long> ids);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM CartItem ci WHERE ci.cart.member.id = :memberId AND ci.product.id IN :productIds")
    int deleteByMemberIdAndProductIds(@Param("memberId") Long memberId, @Param("productIds") List<Long> productIds
    );
}