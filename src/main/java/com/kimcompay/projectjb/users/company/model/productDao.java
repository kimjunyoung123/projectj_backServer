package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface productDao extends JpaRepository<productVo,Integer> {
    
    List<productVo>findByFlyerId(int flyerId);

    @Query(value = "select count(*) from flyers where flyer_id=?",nativeQuery = true)
    int countFlyerByFlyerId(int flyerId);

    @Query(value = "select a.*,b.product_event_id,b.product_event_date,b.product_event_price "
    +"from products a left join product_events b on a.product_id=b.product_id "
    +"where a.product_id=?",nativeQuery = true)
    List<Map<String,Object>>findByIdJoinEvent(int productId);
}   
