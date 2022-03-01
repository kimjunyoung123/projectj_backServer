package com.kimcompay.projectjb.payments.model.basket;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface basketDao extends JpaRepository<basketVo,Integer> {
    
    @Query(value = "select * ,(select count(*) from baskets where user_id=?)totalCount"
    +" from baskets where user_id=? order by basket_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserId(int userId,int sameUserId,int start,int pageSize);

    @Modifying
    @Transactional
    @Query(value = "delete from baskets where basket_id=? and user_id=?",nativeQuery = true)
    void deleteIdAndUserId(int basketId,int userId);
}
