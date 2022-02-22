package com.kimcompay.projectjb.users.company.model.flyers;

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
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
@Table(name = "flyer_details")
@Entity
public class flyerDetailVo {
    
    @Id 
    @Column(name = "flyer_details_id",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "flyer_id")
    private int flyerId;

    @Column(name = "flyer_img_path",nullable = false)
    private String imgPath;

    @Column(name = "flyer_detail_default",columnDefinition = "TINYINT")
    private int defaultFlag;

}
