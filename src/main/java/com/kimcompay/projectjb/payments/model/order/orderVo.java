package com.kimcompay.projectjb.payments.model.order;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "orders")
@Entity
public class orderVo {
    
    @Id
    @Column(name = "order_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "product_id",nullable = false)
    private int productId;

    @Column(name ="mchtTrdNo",nullable = false)
    private String mchtTrdNo;

    @Column(name ="order_coupon")
    private String coupon;

    @Column(name = "order_price",nullable = false)
    private int price;

    @Column(name = "store_id",nullable = false)
    private int storeId;

    @Column(name = "basket_id",nullable = false)
    private int basketId;

    @Column(name = "user_id",nullable = false)
    private int userId;

    @Column(name = "order_count",nullable = false)
    private int count;

    @Column(name = "oder_cancle_flag",columnDefinition = "TINYINT")
    private int cancleFlag;

    @Column(name = "order_created" )
    @CreationTimestamp
    private Timestamp created;

}
