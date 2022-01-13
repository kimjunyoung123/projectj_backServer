package com.kimcompay.projectjb.users.company.model;

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

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "flyers")
@Entity
public class flyerVo {

    @Id 
    @Column(name = "fid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fid;

    @Column(name = "fcompany_num",nullable = false)
    private String companyNum;

    @Column(name = "fimg_path",nullable = false)
    private String fImg_path;

    @Column(name = "fcreated")
    @CreationTimestamp
    private Timestamp fcreated;

    @Column(name = "default_select",columnDefinition = "TINYINT")
    private int defaultSelect;

}
