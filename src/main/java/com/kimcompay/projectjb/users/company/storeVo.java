package com.kimcompay.projectjb.users.company;

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
@Table(name = "stores")
@Entity
public class storeVo {
    
    @Id 
    @Column(name = "sid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sid;

    @Column(name = "saddress",nullable = false ,length = 50)
    private String saddress;

    @Column(name = "sdetail_address",nullable = false ,length = 50)
    private String sdetail_address;

    @Column(name = "spostcode",nullable = false,length = 20 )
    private String spostcode;

    @Column(name = "sphone",nullable = false ,unique = true,length = 11)
    private String sphone;

    @Column(name = "stel",nullable = false ,unique = true,length = 11)
    private String stel;

    @Column(name = "sname",nullable = false ,length = 50)
    private String sname;
    
    @Column(name = "snum",nullable = false ,length = 50)
    private String snum;

    @Column(name = "simg",nullable = false ,length = 255)
    private String simg;


    @Column(name = "openTime",nullable = false ,length = 20)
    private String openTime;

    @Column(name = "clodseTime",nullable = false ,length = 20)
    private String clodseTime;

    @Column(name = "screated")
    @CreationTimestamp
    private Timestamp screated;

    @Column(name = "ssleep",nullable = false ,columnDefinition = "TINYINT")
    private int ssleep;

}
