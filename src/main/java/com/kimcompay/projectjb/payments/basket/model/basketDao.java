package com.kimcompay.projectjb.payments.basket.model;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface basketDao extends JpaRepository<basketVo,Integer> {
    
    @Query(value = "select a.* ,(select count(*) from baskets where user_id=?)totalCount,b.product_name,b.product_img_path"
    +" from baskets a inner join products b on a.product_id=b.product_id where a.user_id=? order by a.basket_id limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserId(int userId,int sameUserId,int start,int pageSize);

}
