package com.kimcompay.projectjb.payments.model.kpay;

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
@Table(name = "kpays")
@Entity
public class kpayVo {
    @Id
    @Column(name = "kpay_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "kpay_mchtTrdNo")
    private String mchtTrdNo;

    @Column(name = "kpay_payment_id")
    private int paymentId;

    
}
