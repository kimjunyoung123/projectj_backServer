package com.kimcompay.projectjb.payments.model.coupon;

import org.springframework.data.jpa.repository.JpaRepository;

public interface couponDao extends JpaRepository<couponVo,Integer> {
    
}
