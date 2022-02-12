package com.kimcompay.projectjb.users.company.model;

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
@Builder
@Data
@Table(name = "product_events")
@Entity
public class productEventVo {
    
    @Id 
    @Column(name = "product_event_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "product_event_date",nullable = false)
    private String date;

    @Column(name = "product_event_price",nullable = false)
    private int eventPrice;

    @Column(name = "product_id",nullable = false)
    private int productId;

    
}
