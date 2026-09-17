package com.example.commerceplus.domain.product.repository;

import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionRequest;
import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionResponse;
import com.example.commerceplus.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepositoryCustom {

    Page<SearchProductConditionResponse> findProductsByCondition(Pageable pageable, SearchProductConditionRequest condition);

}
