
# 인덱스 

테스트 결과 인덱스는 3번 테스트에서 가장 좋은 평가를 받은 (category,created_at)로 선택을 하였다.

이유는 시나리오 A, B에 대하여 행이 5만 -> 5천으로 줄었으며
Using filesort 가 가장 적으면서 인덱스를 넣어야 할게 category와 created_at 밖에 없기 때문이다.

물론, 전체 검색이나 인덱스가 없는 조건으로 검색시 범위를 좁힐 조건이 없기 때문에 인덱스가 없는거랑 속도가 동일하다

```
ALTER TABLE products ADD INDEX idx_category_created (category, created_at);

```

■ 1. 테스트 상태: 인덱스 없음

1. 시나리오 A (카테고리+가격+정렬)
   - type: all / rows: 49820 / Extra: Using where; Using filesort / time=26

2. 시나리오 B (카테고리+정렬)
   - type: all / rows: 49820 / Extra: Using where; Using filesort / time=30.4

3. 시나리오 C (가격만+정렬)
   - type: ALL  / rows: 49820 / Extra: Using where; Using filesort / time=32.8

---

■ 2. 테스트 상태: 인덱스 (category,price) 적용 후

1. 시나리오 A (카테고리+가격+정렬)
    - type: range / rows: 192 / Extra: Using index condition; Using filesort / time=23.9

2. 시나리오 B (카테고리만+정렬)
    - type: ref / rows: 5032 / Extra: Using index condition; Using filesort / time=20.7

3. 시나리오 C (가격만+정렬)
    - type: ALL  / rows: 49820 / Extra: Using where; Using filesort / time=24.8

---

■ 3. 테스트 상태: 인덱스 (category,created_at) 적용 후 (선택)

1. 시나리오 A (카테고리+가격+정렬)
   - type: ref / rows: 5032 / Extra: Using index condition; Using where; Backward index scan / time 2.33
   
2. 시나리오 B (카테고리만+정렬)
   - type: ref / rows: 5032 / Extra: Using where; Backward index scan / time 0.401

3. 시나리오 C (가격만+정렬)
   - type: ALL  / rows: 49820 / Extra: Using where; Using filesort / time 28.28

■ 3-1. 인덱스 (category,created_at) 조건을 좁게

type: ref / rows: 4945 / Extra: Using where; Backward index scan / time=24.1

■ 인덱스 (category,created_at) 조건을 넓게

type: ref / rows: 5088 / Extra: Using where; Backward index scan / time=1.47

---

■ 4. 테스트 상태: 인덱스 (category,created_at, price) 적용 후

1. 시나리오 A (카테고리+가격+정렬)
   - type: ref / rows: 5032 / Extra: Using where; Backward index scan / time 4.05

2. 시나리오 B (카테고리만+정렬)
   - type: ref / rows: 5032 / Extra: Using where; Backward index scan / time 0.392

3. 시나리오 C (가격만+정렬)
   - type: ALL  / rows: 49820 / Extra: Using where; Using filesort / time 29.5

---

# 캐시 

전체 상품 조회 dto는 모든 클라이언트의 첫 페이지에서 항상 조회가 되는 데이터이고
조건 검색과 page 조회 또한 가장 많이 일어나는 조회이기 때문에 캐싱을 선택하였다.

물론, 전체 상품 조회에서 캐시 불일치 확률이 큰 '재고'는 캐시에서 제외하고
상품 단건 조회에만 상품의 재고를 전달하게 하였다.


---

■ 1. 캐시가 없는 조회

1. 첫 번째 조회 : 651ms
2. 두 번쨔 조회 : 66ms
3. 세 번째 조회 : 35ms


■ 2. 캐시가 있는 조회

1. 첫 번째 조회 : 508ms
2. 두 번쨔 조회 : 25ms
3. 세 번째 조회 : 6ms