package com.kimcompay.projectjb.users.company.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface flyerDao extends JpaRepository<flyerVo,Integer> {
    
    //@Query(value = "select * from flyers where store_id=?",nativeQuery = true)
    List<flyerVo>findByStoreId(int storeId);
}
