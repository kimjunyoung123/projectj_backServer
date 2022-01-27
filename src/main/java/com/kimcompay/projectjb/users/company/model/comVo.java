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
@Builder
@Data
@ToString
@Table(name = "companys")
@Entity
public class comVo {

    @Id 
    @Column(name = "cid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cid;

    @Column(name = "company_email",nullable = false,unique = true,length = 50)
    private String cemail;

    @Column(name = "company_pwd",nullable = false ,length = 200)
    private String cpwd;

    @Column(name = "company_address",nullable = false ,length = 50)
    private String caddress;

    @Column(name = "company_detail_address",nullable = false ,length = 50)
    private String cdetail_address;

    @Column(name = "company_postcode",nullable = false,length = 20 )
    private String cpostcode;

    @Column(name = "company_phone",nullable = false ,unique = true,length = 11)
    private String cphone;

    @Column(name = "company_tel",nullable = false ,unique = true,length = 11)
    private String ctel;

    @Column(name = "company_role",nullable = false ,length = 20)
    private String crole;

    @Column(name = "company_name",nullable = false ,length = 100)
    private String store_name;

    @Column(name = "company_login_date")
    @CreationTimestamp
    private Timestamp clogin_date;

    @Column(name = "company_created")
    @CreationTimestamp
    private Timestamp ccreated;

    @Column(name = "company_sleep",nullable = false ,columnDefinition = "TINYINT")
    private int csleep;

    @Column(name = "company_num",nullable = false,unique = true)
    private String cnum;

    @Column(name = "company_kind",nullable = false,columnDefinition = "TINYINT")
    private int ckind;
}
