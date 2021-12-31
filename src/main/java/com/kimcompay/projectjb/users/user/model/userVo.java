package com.kimcompay.projectjb.users.user;

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

    @Column(name = "uemail",nullable = false,unique = true,length = 50)
    private String email;

    @Column(name = "upwd",nullable = false ,length = 200)
    private String upwd;

    @Column(name = "uaddress",nullable = false ,length = 50)
    private String uaddress;

    @Column(name = "udetail_address",nullable = false ,length = 50)
    private String udetail_address;

    @Column(name = "upostcode",nullable = false ,length =20)
    private String upostcode;

    @Column(name = "uphone",nullable = false ,length = 11)
    private String uphone;

    @Column(name = "ulogin_date")
    @CreationTimestamp
    private Timestamp ulogin_date;

    @Column(name = "ucreated")
    @CreationTimestamp
    private Timestamp ucreated;

    @Column(name = "usleep",nullable = false ,columnDefinition = "TINYINT")
    private int usleep;

    @Column(name = "urole",nullable = false ,length = 20)
    private String urole;

    @Column(name = "ubirth",nullable = false ,length = 30)
    private String ubirth;




    

}
