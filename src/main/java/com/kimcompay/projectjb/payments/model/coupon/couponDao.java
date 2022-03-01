package com.kimcompay.projectjb.payments.model.coupon;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface couponDao extends JpaRepository<couponVo,Integer> {
    
    Optional<couponVo> findByName(String couponName);
}
