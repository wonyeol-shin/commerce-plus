package com.example.commerceplus.domain.product.controller;

import com.example.commerceplus.common.annotation.Auth;
import com.example.commerceplus.common.api.ApiResponse;
import com.example.commerceplus.common.api.PageResponse;
import com.example.commerceplus.common.jwt.JwtUser;
import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionRequest;
import com.example.commerceplus.domain.product.dto.request.PatchProductRequest;
import com.example.commerceplus.domain.product.dto.response.GetAllProductResponse;
import com.example.commerceplus.domain.product.dto.response.GetProductResponse;
import com.example.commerceplus.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<GetAllProductResponse>>> getProductAll(
            @Valid SearchProductConditionRequest condition )

    {

        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                productService.findProductAll(pageable, condition),
                GetAllProductResponse::from
        )));
    }

    @GetMapping("/products/cache")
    public ResponseEntity<ApiResponse<PageResponse<GetAllProductResponse>>> getProductAllWithCache(
            @Valid SearchProductConditionRequest condition )
    {
        Pageable pageable = PageRequest.of(condition.page(), condition.size());
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(
                productService.findProductAllWitCache(pageable, condition),
                GetAllProductResponse::from
        )));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<GetProductResponse>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findProduct(productId)));
    }

    @PatchMapping("/api/product/{productId}")
    public ResponseEntity<ApiResponse<GetProductResponse>> PatchProduct(
            @PathVariable Long productId, @Valid @RequestBody PatchProductRequest request)
    {
        return ResponseEntity.ok(ApiResponse.ok(productService.updateProduct(productId, request)));
    }
}
