package com.kimcompay.projectjb.payments.model.pay;

import org.springframework.data.jpa.repository.JpaRepository;

public interface paymentDao extends JpaRepository<paymentVo,Integer> {
    boolean existsByMchtTrdNo(String mchtTrdNo);
    
}
