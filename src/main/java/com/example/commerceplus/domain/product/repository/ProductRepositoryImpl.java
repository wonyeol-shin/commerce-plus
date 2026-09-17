package com.example.commerceplus.domain.product.repository;

import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionRequest;
import com.example.commerceplus.domain.product.dto.condition.SearchProductConditionResponse;
import com.example.commerceplus.domain.product.entity.ProductCategory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.commerceplus.domain.product.entity.QProduct.product;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<SearchProductConditionResponse> findProductsByCondition(Pageable pageable, SearchProductConditionRequest condition) {

        List<SearchProductConditionResponse> products = jpaQueryFactory
                .select(Projections.constructor(SearchProductConditionResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.category

                        ))
                .from(product)
                .where(
                        minPriceCondition(condition.minPrice()),
                        maxPriceCondition(condition.maxPrice()),
                        categoryCondition(condition.category())
                )
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(product.count())
                .from(product)
                .where(
                        minPriceCondition(condition.minPrice()),
                        maxPriceCondition(condition.maxPrice()),
                        categoryCondition(condition.category())
                );

        return PageableExecutionUtils.getPage(products, pageable, countQuery::fetchOne );
    }

    // 최소가격보다 큼
    private BooleanExpression minPriceCondition(Integer minPrice) {
        return minPrice == null ? null : product.price.goe(minPrice);
    }

    // 최대가격보다 작음
    private BooleanExpression maxPriceCondition(Integer maxPrice) {
        return maxPrice == null ? null : product.price.loe(maxPrice);
    }

    private BooleanExpression categoryCondition(ProductCategory category) {
        return category == null ? null : product.category.eq(category);
    }

}
