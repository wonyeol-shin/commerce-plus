package com.example.commerceplus.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    FASHION("패션/의류"),
    BEAUTY("뷰티/화장품"),
    ELECTRONICS("디지털/가전"),
    HOME_LIVING("생활/주방용품"),
    FOOD("식품/건강"),
    FURNITURE("가구/인테리어"),
    SPORTS("스포츠/레저"),
    PETS("반려동물"),
    BOOKS_HOBBY("도서/취미"),
    ETC("기타");

    private final String description;

}
