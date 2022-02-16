package com.kimcompay.projectjb.users.company.model;

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
@Table(name = "flyers")
@Entity
public class flyerVo {

    @Id 
    @Column(name = "flyer_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "store_id",nullable = false)
    private int storeId;

    @Column(name = "company_id",nullable = false)
    private int companyId;

    @Column(name = "flyer_img_path",nullable = false)
    private String img_path;

    @Column(name = "flyer_created")
    @CreationTimestamp
    private Timestamp created;

    @Column(name = "default_select",columnDefinition = "TINYINT")
    private int defaultSelect;

}
