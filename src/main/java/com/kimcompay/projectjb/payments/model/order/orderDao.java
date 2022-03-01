package com.kimcompay.projectjb.payments.model.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface orderDao extends JpaRepository<orderVo,Integer>{
    
}
