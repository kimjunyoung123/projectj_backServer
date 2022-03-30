package com.kimcompay.projectjb.payments.model.pay;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface paymentDao extends JpaRepository<paymentVo,Integer> {
    boolean existsByMchtTrdNo(String mchtTrdNo);
    
    @Query(value = "select a.*,b.*,c.*,d.*,e.order_id,e.order_price,e.order_count"
    +" from payments a left join vbanks b on a.mcht_trd_no=b.vbank_mcht_trd_no left join cards c on a.mcht_trd_no=c.card_mcht_trd_no left join kpays d on a.mcht_trd_no=kpay_mcht_trd_no left join orders e on a.mcht_trd_no=e.order_mcht_trd_no"
    +" where a.user_id=?",nativeQuery = true)
    List<Map<String,Object>>findJoinCardVbankKpayOrder(int userId);

    @Query(value ="select a.*,c.user_email,(select count(a.payment_id) from payments a left join orders b on a.mcht_trd_no = b.order_mcht_trd_no and b.store_id=?)totalCount"
    +" from payments a left join orders b on a.mcht_trd_no = b.order_mcht_trd_no and b.store_id=? left join users c on a.user_id = c.uid"
    +"  order by a.payment_id desc limit ?,?" ,nativeQuery =true)
    List<Map<String,Object>>findJoinByStoreId(int storeId,int sameStoreId,int start,int pageSize);
}
