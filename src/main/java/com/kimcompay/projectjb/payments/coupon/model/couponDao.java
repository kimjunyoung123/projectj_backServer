package com.kimcompay.projectjb.payments.coupon.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface couponDao extends JpaRepository<couponVo,Integer> {
    
}
