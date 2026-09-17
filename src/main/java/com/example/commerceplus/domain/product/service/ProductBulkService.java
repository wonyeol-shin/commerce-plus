package com.example.commerceplus.domain.product.service;

import com.example.commerceplus.domain.product.entity.Product;
import com.example.commerceplus.domain.product.entity.ProductCategory;
import com.example.commerceplus.domain.product.repository.ProductBulkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class ProductBulkService {

    private final ProductBulkRepository productBulkRepository;

    public void createAndSaveBulkProducts() {
        List<Product> products = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. 랜덤 조합용 가짜 데이터 풀(Pool) 정의
        String[] adjectives = {"프리미엄", "친환경", "가성비", "스마트", "감성", "초고속", "한정판", "데일리", "최악인", "비싼"};
        String[] itemNames = {"모니터", "립스틱", "소파", "스낵", "영양제", "티셔츠", "텐트", "디퓨저", "키보드", "게임기"};
        String[] comments = {
                "실제 사용해보니 정말 만족스럽습니다.",
                "지인에게 선물하기 딱 좋은 상품입니다.",
                "배송도 빠르고 품질도 기대 이상이네요.",
                "가성비 원탑 제품으로 추천합니다."
        };
        ProductCategory[] categories = ProductCategory.values(); // Enum 전체 가져오기

        // 2. 5만 번 돌며 랜덤 조합 생성
        for (int i = 1; i <= 50000; i++) {
            // 랜덤 이름 생성 (예: 프리미엄 모니터 482번)
            String name = adjectives[random.nextInt(adjectives.length)] + " " +
                    itemNames[random.nextInt(itemNames.length)] + " " + i;

            // 1,000원 ~ 500,000원 사이 1,000원 단위 랜덤 가격
            int price = random.nextInt(1, 501) * 1000;

            // 1개 ~ 999개 랜덤 재고
            int stock = random.nextInt(1, 1000);

            // 랜덤 코멘트
            String comment = comments[random.nextInt(comments.length)];

            // 랜덤 카테고리 Enum 선택
            ProductCategory category = categories[random.nextInt(categories.length)];

            // 객체 생성 후 리스트에 add
            products.add(Product.create(name, price, stock, comment, category));
        }

        // 3. 쪼개서 저장하는 배치 메서드 호출
        productBulkRepository.saveAllProductByList(products);
    }
}
