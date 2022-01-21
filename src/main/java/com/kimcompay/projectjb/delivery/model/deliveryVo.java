package com.kimcompay.projectjb.delivery.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.auto.value.AutoValue.Builder;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "deliverys")
@Entity
public class deliveryVo {
       
    @Id
    @Column(name = "did",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int did;

    @Column(name = "store_id",nullable = false,length = 50)
    private String storeId;

    @Column(name = "destination_address",nullable = false,length = 50)
    private String destinationAddress;

    @Column(name = "destination_detail_address",nullable = false,length = 50)
    private String destinationDetailAddress;

    @Column(name = "buyer_id",nullable = false)
    private int buyerId;

    @Column(name = "delivery_done_flag",nullable = false ,columnDefinition = "TINYINT")
    private int delivery_done_flag;

    @Column(name = "dcreated")
    @CreationTimestamp
    private Timestamp dcreated;

    @Column(name = "dcancle_date")
    private Timestamp dCancleDate;




}
