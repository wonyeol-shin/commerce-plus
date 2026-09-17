package com.example.commerceplus.domain.product.controller;

import com.example.commerceplus.domain.product.service.ProductBulkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class ProductBulkController {

    private final ProductBulkService productBulkService;

    @PostMapping("/api/products/bulk")
    public ResponseEntity<Void> bulk() {
        productBulkService.createAndSaveBulkProducts();
        return ResponseEntity.ok().build();
    }

}
