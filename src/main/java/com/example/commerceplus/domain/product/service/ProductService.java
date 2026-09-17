package com.example.commerceplus.domain.product.service;

import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionRequest;
import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionResponse;
import com.example.commerceplus.domain.product.dto.request.PatchProductRequest;
import com.example.commerceplus.domain.product.dto.response.GetProductResponse;
import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.TreeMap;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<SearchProductConditionResponse> findProductAll(Pageable pageable, SearchProductConditionRequest condition) {
        if (condition.isMinPriceGreaterThanMaxPrice()) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }
        return productRepository.findProductsByCondition(pageable, condition);
    }

    @Cacheable(value = "product_condition", key = "#condition.getCacheKey()"
    )
    @Transactional(readOnly = true)
    public Page<SearchProductConditionResponse> findProductAllWitCache(Pageable pageable, SearchProductConditionRequest condition) {
        if (condition.isMinPriceGreaterThanMaxPrice()) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }
        return productRepository.findProductsByCondition(pageable, condition);
    }

    @Transactional(readOnly = true)
    public GetProductResponse findProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(()
                -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return GetProductResponse.from(product);
    }

    @CacheEvict(value = "product_condition", allEntries = true)
    public GetProductResponse updateProduct(Long productId, PatchProductRequest request) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        product.updateProduct(request.name(), request.price(), request.comment(), request.category());
        productRepository.save(product);
        return GetProductResponse.from(product);
    }

    // 동시성 제연을 위해 락이 없는 버전
    public Product findProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 동시성을 막기 위한 비관적 락을 사용한 버가
    public Product findProductByIdWithLock(Long productId) {
        Product product = productRepository.findByIdWithLock(productId).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        log.info("Product Service thread={},  productId ={}, productStock ={}",
                Thread.currentThread().getName(), product.getId(),product.getStock());
        return product;
    }

    public void restoreStocks(Map<Long, Integer> quantitiesByProduct) {
        Map<Long, Integer> sortedQuantitiesByProduct = new TreeMap<>(quantitiesByProduct);

        for (Map.Entry<Long, Integer> entry : sortedQuantitiesByProduct.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            //상품마다 한 번 조회하고 한 번 복구
            Product product = findProductByIdWithLock(productId);
            product.restoreStock(quantity);
        }
    }
}
