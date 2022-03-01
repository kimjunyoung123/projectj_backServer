package com.kimcompay.projectjb.payments.model.coupon;

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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "coupons")
@Entity
public class couponVo {
    
    @Id
    @Column(name = "coupon_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "store_id",nullable = false)
    private int storeId;

    @Column(name = "coupon_kind",nullable = false,columnDefinition = "TINYINT")
    private int kind;

    @Column(name = "coupon_name",nullable = false)
    private String name;

    @Column(name = "coupon_num",nullable = false)
    private String num;
    
    @Column(name = "coupon_expire",nullable = false)
    private Timestamp expire;
    
    @Column(name = "coupon_created")
    @CreationTimestamp
    private Timestamp created;


}
