package com.example.commerceplus.domain.product.entity;

import com.example.commerceplus.common.entity.BaseTimeEntity;
import com.example.commerceplus.common.exception.BusinessException;
import com.example.commerceplus.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int price;

    @Column(nullable = false, columnDefinition = "INT UNSIGNED")
    private int stock;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    private Product(String name, int price, int stock, String comment, ProductCategory category) {
        isPriceLessThanZero(price);
        isStockLessThanOne(stock);

        this.name = name;
        this.price = price;
        this.stock = stock;
        this.comment = comment;
        this.category = category;
    }

    public static Product create(String name, int price, int stock, String comment, ProductCategory category) {
        return new Product(name, price, stock, comment, category);
    }

    public void decreaseStock(int stock) {
        isStockLessThanOne(stock);
        log.info("Product Entity thread={}, tx={} productId ={}, productStock ={}, requestStock = {}, totalStock = {} ",
                Thread.currentThread().getName(),
                TransactionSynchronizationManager.isActualTransactionActive(),
                this.id,this.stock, stock, this.stock - stock
        );
        if (this.stock - stock < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        this.stock -= stock;
    }

    public void restoreStock(int stock) {
        isStockLessThanOne(stock);
        this.stock += stock;
    }

    public boolean isEnoughStock(int stock) {
        return this.stock >= stock;
    }

    public void updateProduct(String name, int price, String comment, ProductCategory category){
        this.name = name;
        this.price = price;
        this.comment = comment;
        this.category = category;
    }

    private void isStockLessThanOne(int stock) {
        if (stock < 1) {
            throw new BusinessException(ErrorCode.INVALID_STOCK);
        }
    }

    private void isPriceLessThanZero(int price) {
        if (price < 1) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }
    }

}
