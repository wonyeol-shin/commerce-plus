package com.example.commerceplus.domain.order.controller;

import com.example.commerceplus.domain.order.service.OrderBulkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class OrderBulkController {

    private final OrderBulkService orderBulkService;

    @PostMapping("/api/orders/bulk")
    public ResponseEntity<Void> bulk() {
        orderBulkService.createAndSaveBulkOrders();
        return ResponseEntity.ok().build();
    }
}
