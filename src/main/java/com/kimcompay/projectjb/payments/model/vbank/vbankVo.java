package com.kimcompay.projectjb.payments.model.vbank;

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
@Table(name = "vbanks")
@Entity
public class vbankVo {
    
    @Id
    @Column(name = "vbank_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "vbank_mchtId",nullable = false)
    private String mchtId;

    @Column(name = "vbank_mchtTrdNo",nullable = false)
    private String mchtTrdNo;

    @Column(name = "vbank_trdNo",nullable = false)
    private String trdNo;

    @Column(name = "vtlAcntNo",nullable = false)
    private String vtlAcntNo;

    @Column(name = "vbank_fnNm",nullable = false)
    private String fnNm;

    @Column(name = "vbank_fnCd",nullable = false)
    private String fnCd; 
    
    @Column(name = "vbank_cnclOrd")
    private int cnclOrd; 

    @Column(name = "expireDt",nullable = false)
    private Timestamp expireDt;

    @Column(name = "vbank_trdDtm")
    private Timestamp vtrdDtm;

    @Column(name = "vbank_status",nullable = false)
    private String status;

    @Column(name = "vbank_created")
    @CreationTimestamp
    private Timestamp created;   
}
