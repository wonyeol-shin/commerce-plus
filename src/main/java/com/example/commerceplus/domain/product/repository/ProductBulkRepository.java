package com.example.commerceplus.domain.product.repository;

import com.example.commerceplus.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    // insert 받을값을 list로 받음
    public void saveAllProductByList(List<Product> products) {

        // 칼럼부분이 ? 와 매칭하여 값 삽입
       String sql = "INSERT INTO products " +
               "(name, price, stock, comment, category, created_at, modified_at) " +
               "VALUES(?, ?, ?, ?, ?, NOW(6), NOW(6))";

       // 리스트에 있는 모든값을 다 insert하기 보다 1,000개씩 줄이기
       int batchSize = 1_000;
       int totalSize = products.size();

       // 0 ~ totalSize 만큼 반복
       for (int i=0; i< totalSize; i+=batchSize) {
           // 리스트를 정해진 개수(1,000개) 만큼 쪼개기
           List<Product> subList = products.subList(i, Math.min(i+batchSize, totalSize));

           jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

               @Override
               public void setValues(PreparedStatement ps, int i) throws SQLException {
                   // 리스트에서 값을 꺼냄
                   Product product = subList.get(i);
                   // ? 의 순서에 맞게 삽입
                   ps.setString(1, product.getName());
                   ps.setInt(2, product.getPrice());
                   ps.setInt(3, product.getStock());
                   ps.setString(4, product.getComment());
                   ps.setString(5, product.getCategory().name());
               }

               // 넣으려는 전체 데이터 개수를 반환, 이 수만큼 넣기
               @Override
               public int getBatchSize() {
                   return subList.size();
               }

           });

        }


    }

}
