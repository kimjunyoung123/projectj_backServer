package com.kimcompay.projectjb.users.company.model.stores;

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
@Builder
@Data
@Table(name = "store_reviews")
@Entity
public class storeReviewVo {
    
    @Id 
    @Column(name = "store_review_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "store_id",nullable = false)
    private int storeId;

    @Column(name = "store_review_text",nullable = false,length = 2000)
    private String text;

    @Column(name = "store_review_writer",nullable = false)
    private int userId;

    @Column(name = "store_review_created")
    @CreationTimestamp
    private Timestamp created;
}
