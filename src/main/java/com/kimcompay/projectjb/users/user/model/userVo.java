package com.kimcompay.projectjb.users.user.model;

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
@Table(name = "users")
@Entity
public class userVo {
    
    @Id
    @Column(name = "uid",nullable = false,unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    @Column(name = "user_email",nullable = false,unique = true,length = 50)
    private String email;

    @Column(name = "user_pwd",nullable = false ,length = 200)
    private String upwd;

    @Column(name = "user_address",nullable = false ,length = 50)
    private String uaddress;

    @Column(name = "user_detail_address",nullable = false ,length = 50)
    private String udetail_address;

    @Column(name = "user_postcode",nullable = false ,length =20)
    private String upostcode;

    @Column(name = "user_phone",nullable = false ,length = 11)
    private String uphone;

    @Column(name = "user_login_date")
    @CreationTimestamp
    private Timestamp ulogin_date;

    @Column(name = "user_created")
    @CreationTimestamp
    private Timestamp ucreated;

    @Column(name = "user_sleep",nullable = false ,columnDefinition = "TINYINT")
    private int usleep;

    @Column(name = "user_role",nullable = false ,length = 20)
    private String urole;

    @Column(name = "user_birth",nullable = false ,length = 30)
    private String ubirth;

    @Column(name = "provider",nullable = false,length = 30)
    private String provider;




    

}
