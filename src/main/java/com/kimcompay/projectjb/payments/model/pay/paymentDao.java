package com.kimcompay.projectjb.payments.model.pay;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface paymentDao extends JpaRepository<paymentVo,Integer> {
    boolean existsByMchtTrdNo(String mchtTrdNo);
    
    @Query(value = "select a.*,e.order_id,e.order_price,e.order_count,e.order_coupon,(select count(*) from payments where user_id=?)totalCount"
    +" from payments a left join orders e on a.mcht_trd_no=e.order_mcht_trd_no"
    +" where a.user_id=? order by a.payment_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findJoinCardVbankKpayOrder(int userId,int sameUserId,int start,int pageSize);

    @Query(value = "select a.*,e.order_id,e.order_price,e.order_count,e.order_coupon,(select count(*) from payments where user_id=? and payment_created between ? and ?)totalCount"
    +" from payments a left join orders e on a.mcht_trd_no=e.order_mcht_trd_no"
    +" where a.user_id=? and a.payment_created between ? and ? order by a.payment_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findJoinCardVbankKpayOrder(int userId,Timestamp startDate,Timestamp endDate,int sameUserId,Timestamp sameStartDate,Timestamp sameEndDate,int start,int pageSize);
}
