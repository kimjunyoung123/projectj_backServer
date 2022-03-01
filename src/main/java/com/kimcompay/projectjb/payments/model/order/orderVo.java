package com.kimcompay.projectjb.payments.model.order;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "orders")
@Entity
public class orderVo {
    
    @Id
    @Column(name = "order_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "product_id",nullable = false)
    private int productId;

    @Column(name = "product_name",nullable = false)
    private String productName;

    @Column(name ="method",nullable = false)
    private String method;

    @Column(name ="mchtTrdNo",nullable = false)
    private String mchtTrdNo;

    @Column(name ="soldout_action",nullable = false)
    private String soldOurAction;

    @Column(name = "user_id",nullable = false)
    private int userId;

    @Column(name = "order_created" )
    private Timestamp created;

}
