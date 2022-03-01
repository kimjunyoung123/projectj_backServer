package com.kimcompay.projectjb.payments.basket.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface basketDao extends JpaRepository<basketVo,Integer> {
    
    @Query(value = "select * ,(select count(*) from baskets where user_id=?)totalCount"
    +" from baskets where user_id=? order by basket_id limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserId(int userId,int sameUserId,int start,int pageSize);

}
