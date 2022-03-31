package com.kimcompay.projectjb.payments.model.pay;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface paymentDao extends JpaRepository<paymentVo,Integer> {
    boolean existsByMchtTrdNo(String mchtTrdNo);
    
    @Query(value = "select a.*,b.*,c.*,d.*,e.order_id,e.order_price,e.order_count"
    +" from payments a left join vbanks b on a.mcht_trd_no=b.vbank_mcht_trd_no left join cards c on a.mcht_trd_no=c.card_mcht_trd_no left join kpays d on a.mcht_trd_no=kpay_mcht_trd_no left join orders e on a.mcht_trd_no=e.order_mcht_trd_no"
    +" where a.user_id=?",nativeQuery = true)
    List<Map<String,Object>>findJoinCardVbankKpayOrder(int userId);

    @Query(value ="SELECT DISTINCT a.mcht_trd_no as ad,a.*,d.vbank_status,"
    +"(select count(DISTINCT a.payment_id) FROM payments a left join orders b on a.mcht_trd_no = b.order_mcht_trd_no and b.store_id=?)totalCount,c.user_email"
    +" FROM payments a left join orders b on a.mcht_trd_no = b.order_mcht_trd_no and b.store_id=? left join users c on c.uid=b.user_id left join vbanks d on a.mcht_trd_no = d.vbank_mcht_trd_no "
    +" order by a.payment_id desc limit ?,?" ,nativeQuery =true)
    List<Map<String,Object>>findJoinByStoreId(int storeId,int sameStoreId,int start,int pageSize);

    @Query(value = "SELECT b.payment_id,b.method,b.soldout_action,b.payment_total_price,b.payment_created,b.cancle_all_flag,b.cncl_ord"
    +",a.*,c.*,d.*,e.*"
    +" FROM orders a inner join payments b on a.order_mcht_trd_no=b.mcht_trd_no"
    +" left join vbanks c on a.order_mcht_trd_no=c.vbank_mcht_trd_no"
    +" left join cards d on a.order_mcht_trd_no=d.card_mcht_trd_no"
    +" left join kpays e on a.order_mcht_trd_no=e.kpay_mcht_trd_no"
    +" where a.order_id=? and a.store_id=?",nativeQuery = true)
    Map<String,Object>findByJoinCardVbankKpayAndPayment(int orderId,int storeId);

    @Modifying
    @Transactional
    @Query(value = "update payments set cncl_ord=?,payment_total_price=? where mcht_trd_no=?",nativeQuery = true)
    void updatePriceAndCancleTime(int cancleTime,int totalPrice,String mchtTrdNo);

    @Modifying
    @Transactional
    @Query(value = "update payments set cncl_ord=?,payment_total_price=?,cancle_all_flag=? where mcht_trd_no=?",nativeQuery = true)
    void updatePriceAndCancleTimeZero(int cancleTime,int totalPrice,String mchtTrdNo,int one);
}
