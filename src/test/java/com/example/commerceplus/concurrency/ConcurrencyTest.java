package com.example.commerceplus.concurrency;

import com.example.commerceplus.domain.cart.entity.Cart;
import com.example.commerceplus.domain.cart.entity.CartItem;
import com.example.commerceplus.domain.cart.repository.CartItemRepository;
import com.example.commerceplus.domain.cart.repository.CartRepository;
import com.example.commerceplus.domain.member.entity.Member;
import com.example.commerceplus.domain.member.repository.MemberRepository;
import com.example.commerceplus.domain.order.dto.request.CreateOrderRequest;
import com.example.commerceplus.domain.order.repository.OrderRepository;
import com.example.commerceplus.domain.order.service.OrderFacade;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.entity.ProductCategory;
import com.example.commerceplus.domain.product.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager em; //

    @Test
    void 재고_불변식을_검증한다() throws InterruptedException {
        // given ( 같은 상품을 다른 Member, Cart, CartItem으로 주문 )
        int threadCount = 100;
        int initialStock = 10;
        long beforeOrderCount = orderRepository.count();

        Product product =
                productRepository.saveAndFlush(Product.create("test1", 100, initialStock, "test1", ProductCategory.ETC));
        List<Long> membersIds = new ArrayList<>();
        List<CreateOrderRequest> request = new ArrayList<>();

        for (int i=0; i<threadCount; i++) {
            Member member =
                    memberRepository.saveAndFlush(Member.createNormalMember("test" + i, "1234", "test" + i, "011-1112-111" + i));
            Cart cart = cartRepository.saveAndFlush(Cart.create(member));
            CartItem cartItem = cartItemRepository.saveAndFlush(CartItem.createCartItem(cart, product, 1));

            membersIds.add(member.getId());
            request.add(new CreateOrderRequest(List.of(cartItem.getId())));
        }

        em.clear();

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 👈 래치 1개만 사용

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.execute(() -> { // 👈 execute() 사용
                try {
                    orderFacade.createOrder(membersIds.get(index), request.get(index));
                } catch (Exception e) {
                    log.info("결제 실패 : {}", e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        // then
        countDownLatch.await();
        executorService.shutdown();

        // 성공한 주문의 개수
        long successOrderCount = orderRepository.count() - beforeOrderCount;
        Product resultProduct = productRepository.findById(product.getId()).orElseThrow();

        log.info("성공한 주문 수 = {}", successOrderCount);
        log.info("남은 재고 = {}", resultProduct.getStock());

        assertThat(successOrderCount).isLessThanOrEqualTo(initialStock);
        assertThat(resultProduct.getStock())
                .isEqualTo(initialStock - successOrderCount);
    }

}
