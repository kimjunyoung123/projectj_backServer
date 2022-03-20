package com.kimcompay.projectjb.payments.model.order;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface orderDao extends JpaRepository<orderVo,Integer>{
    @Query(value = "select a.*,(select count(*) from orders where user_id=?)totalCount,b.product_name,b.product_img_path,c.store_name"
    +" from orders a left join products b on a.product_id=b.product_id left join stores c on a.store_id=c.store_id"
    +" where a.user_id=? order by a.order_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserIdPagIng(int userId,int sameUserId,int start,int pageSize);

    @Query(value = "select a.*,(select count(*) from orders where user_id=? and order_created between ? and ?)totalCount,b.product_name,b.product_img_path,b.store_id,c.store_name"
    +" from orders a left join products b on a.product_id=b.product_id left join stores c on b.store_id=c.store_id"
    +" where a.user_id=? and a.order_created between ? and ? order by a.order_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserIdPagIng(int userId,Timestamp startDate,Timestamp endDate,int sameUserId,Timestamp sameStartDate,Timestamp sameEndDate,int start,int pageSize);
}
