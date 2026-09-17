package com.example.commerceplus.domain.order.controller;

import com.example.commerceplus.common.annotation.Auth;
import com.example.commerceplus.common.api.ApiResponse;
import com.example.commerceplus.common.api.PageResponse;
import com.example.commerceplus.common.jwt.JwtUser;
import com.example.commerceplus.domain.order.dto.request.CreateOrderRequest;
import com.example.commerceplus.domain.order.dto.request.SearchOrderConditionRequest;
import com.example.commerceplus.domain.order.dto.response.*;
import com.example.commerceplus.domain.order.service.OrderFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    // 주문서 미리보기
    @GetMapping("/checkout")
    public ResponseEntity<ApiResponse<GetCheckoutResponse>> getCheckoutOne(
            @Auth JwtUser jwtUser,
            @RequestParam(required = true) List<Long> cartItemIds
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderFacade.getCheckoutOne( jwtUser.id(), cartItemIds == null ? List.of() : cartItemIds)
        ));
    }

    // 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Auth JwtUser jwtUser,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderFacade.createOrder( jwtUser.id(), request)));
    }

    // 내 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GetAllOrderResponse>>> getOrdersAll(
            @Auth JwtUser jwtUser,
            @Valid SearchOrderConditionRequest condition
            ) {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                orderFacade.getOrdersAll( jwtUser.id(), pageable),
                order -> order
                )));
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<GetOrderResponse>> getOrderOne(
            @Auth JwtUser jwtUser,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(orderFacade.getOrderOne( jwtUser.id(), orderId)));
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(
            @Auth JwtUser jwtUser,
            @PathVariable Long orderId
    ) {
        CancelOrderResponse response =
                orderFacade.cancelOrder( jwtUser.id(), orderId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}


