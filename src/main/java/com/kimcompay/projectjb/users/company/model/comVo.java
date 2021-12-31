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

    @Column(name = "cemail",nullable = false,unique = true,length = 50)
    private String cemail;

    @Column(name = "cpwd",nullable = false ,length = 200)
    private String cpwd;

    @Column(name = "caddress",nullable = false ,length = 50)
    private String caddress;

    @Column(name = "cdetail_address",nullable = false ,length = 50)
    private String cdetail_address;

    @Column(name = "cpostcode",nullable = false,length = 20 )
    private String cpostcode;

    @Column(name = "cphone",nullable = false ,unique = true,length = 11)
    private String cphone;

    @Column(name = "ctel",nullable = false ,unique = true,length = 11)
    private String ctel;

    @Column(name = "crole",nullable = false ,length = 20)
    private String crole;

    @Column(name = "store_name",nullable = false ,length = 100)
    private String store_name;

    @Column(name = "clogin_date")
    @CreationTimestamp
    private Timestamp clogin_date;

    @Column(name = "ccreated")
    @CreationTimestamp
    private Timestamp ccreated;

    @Column(name = "csleep",nullable = false ,columnDefinition = "TINYINT")
    private int csleep;

    @Column(name = "cnum",nullable = false,unique = true)
    private String cnum;

    @Column(name = "ckind",nullable = false,columnDefinition = "TINYINT")
    private int ckind;
}
