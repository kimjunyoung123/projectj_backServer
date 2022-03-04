package com.kimcompay.projectjb.payments.model.card;

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
@Table(name = "cards")
@Entity
public class cardVo {
    
    @Id
    @Column(name = "card_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "card_mchtTrdNo",nullable = false)
    private String mchtTrdNo;

    @Column(name = "card_payment_id")
    private int paymentId;

    @Column(name = "card_orgTrdNo",nullable = false)
    private String orgTrdNo;

    @Column(name = "card_authNo",nullable = false)
    private String authNo;

    @Column(name = "card_intMon")
    private int intMon;

    @Column(name = "card_fnNm")
    private String fnNm;

    @Column(name="card_fnCd")
    private int fnCd;

    @Column(name = "card_cancle_flag",columnDefinition = "TINYINT")
    private int cancleFlag;

    @Column(name = "card_created")
    @CreationTimestamp
    private Timestamp created;


}
