package com.kimcompay.projectjb.payments.model.order;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface orderDao extends JpaRepository<orderVo,Integer>{
    @Query(value = "select *,(select count(*) from orders where user_id=?)totalCount"
    +" from orders"
    +" where user_id=? order by order_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserIdPagIng(int userId,int sameUserId,int start,int pageSize);

    @Query(value = "select *,(select count(*) from orders where user_id=? and order_created between ? and ?)totalCount"
    +" from orders"
    +" where user_id=? and order_created between ? and ? order by order_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByUserIdPagIng(int userId,Timestamp startDate,Timestamp endDate,int sameUserId,Timestamp sameStartDate,Timestamp sameEndDate,int start,int pageSize);
}
