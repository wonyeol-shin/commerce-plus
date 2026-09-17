package com.example.commerceplus.domain.order.repository;

import com.example.commerceplus.domain.order.dto.response.GetOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Page<GetOrderResponse> findOrdersByMemberId(
            Long memberId,
            Pageable pageable
    );
}

