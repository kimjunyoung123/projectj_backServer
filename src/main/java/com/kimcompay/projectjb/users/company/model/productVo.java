package com.kimcompay.projectjb.users.company.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
@Table(name = "products")
@Entity
public class productVo {  
    
    @Id 
    @Column(name = "pid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pid;

    @Column(name = "store_id",nullable = false)
    private int storeId;

    @Column(name = "flyer_id",nullable = false)
    private int flyer_id;

    @Column(name = "event_state",columnDefinition = "TINYINT")
    private int eventFlag;
    
    @Column(name = "product_name" ,nullable = false ,length = 100)
    private String productName;

    @Column(name = "origin",nullable = false)
    private String origin;

    @Column(name="price",nullable = false)
    private int price;

    @Column(name = "text",nullable = true)
    private String text;

    @Column(name = "product_img_path",nullable = false)
    private String productImgPath;

    @Column(name = "category",nullable =  false)
    private String category;
}
