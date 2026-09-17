package com.example.commerceplus.domain.cart.service;

import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.repository.CartRepository;
import com.example.commerceplus.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;

  public Cart findOrCreateCart (Member member) {
    return cartRepository.findByMemberId(member.getId())
            .orElseGet(() -> cartRepository.save(Cart.create(member)));
  }

  @Transactional(readOnly = true)
  public Optional<Cart> findCart (Long memberId) {
    return cartRepository.findByMemberId(memberId);
  }
}


