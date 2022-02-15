package com.kimcompay.projectjb.users.company.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface flyerDao extends JpaRepository<flyerVo,Integer> {
    
    @Query(value = "select *,(select count(*) from flyers where store_id=?)totalCount from flyers where store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByStoreId(int storeId,int sameStoreid,int page,int pageSize);
    
    @Query(value = "select *,(select count(*) from flyers where flyer_created between ? and ? and store_id=?)totalCount from flyers where flyer_created between ? and ? and store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByDay(Timestamp start,Timestamp end,int storeId,Timestamp sameStart,Timestamp sameEnd,int sameStoreId,int page,int pageSize);

    @Query(value = "select a.*,b.*,c.* from jangbogo.flyers a left join jangbogo.products b on a.flyer_id=b.flyer_id left join jangbogo.product_events c on b.product_id=c.product_id where a.flyer_id=?",nativeQuery = true)
    Map<String,Object>findByFlyerJoinProductAndEvent(int flyerId);
}
