package com.kimcompay.projectjb.users.company.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface flyerDao extends JpaRepository<flyerVo,Integer> {
    
    @Query(value = "select *,(select count(*) from flyers where store_id=?)totalCount from flyers where store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByStoreId(int storeId,int sameStoreid,int page,int pageSize);
    
    @Query(value = "select *,(select count(*) from flyers where flyer_created between ? and ? and store_id=?)totalCount from flyers where flyer_created between ? and ? and store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByDay(Timestamp start,Timestamp end,int storeId,Timestamp sameStart,Timestamp sameEnd,int sameStoreId,int page,int pageSize);

    @Query(value = "select b.*,c.product_event_id,c.product_event_date,c.product_event_price from  products b left join product_events c on b.product_id=c.product_id where b.flyer_id=?",nativeQuery = true)
    List<Map<String,Object>>findByFlyerJoinProductAndEvent(int flyerId);

    Optional<flyerVo> findByFid(int flyerId);
}
