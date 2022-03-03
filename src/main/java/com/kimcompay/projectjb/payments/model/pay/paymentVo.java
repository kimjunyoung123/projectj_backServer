package com.kimcompay.projectjb.payments.model.pay;

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
@Builder
@Data
@ToString
@Table(name = "payments")
@Entity
public class paymentVo {
    
    @Id
    @Column(name = "payment_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="method",nullable = false)
    private String method;

    @Column(name ="mchtTrdNo",nullable = false ,unique = true)
    private String mchtTrdNo;

    @Column(name ="soldout_action",nullable = false)
    private String soldOurAction;

    @Column(name = "user_id",nullable = false)
    private int userId;

    @Column(name = "payment_totalPrice",nullable = false)
    private int totalPrice;

    @Column(name = "cnclOrd",columnDefinition = "TINYINT")
    private int cnclOrd;

    @Column(name = "cancle_all_flag",columnDefinition = "TINYINT")
    private int cancleAllFlag;

    @Column(name = "payment_address")
    private String address;

    @Column(name = "payment_postcode")
    private String postcode;

    @Column(name = "payment_detail_address")
    private String detailAddress;

    @Column(name = "payment_created" )
    @CreationTimestamp
    private Timestamp created;
}
