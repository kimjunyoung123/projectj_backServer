package com.kimcompay.projectjb.users.company.model.flyers;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface flyerDao extends JpaRepository<flyerVo,Integer> {
    
    @Query(value = "select *,(select count(*) from flyers where store_id=?)totalCount from flyers where store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByStoreId(int storeId,int sameStoreid,int page,int pageSize);
    
    @Query(value = "select *,(select count(*) from flyers where flyer_created between ? and ? and store_id=?)totalCount from flyers where flyer_created between ? and ? and store_id=? order by flyer_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByDay(Timestamp start,Timestamp end,int storeId,Timestamp sameStart,Timestamp sameEnd,int sameStoreId,int page,int pageSize);

    @Query(value = "select a.*,b.product_id,b.event_state,b.price,b.product_img_path,b.product_name,b.origin,c.flyer_img_path,c.flyer_details_id,c.flyer_detail_default"
    +" from flyers a left join products b on a.flyer_id=b.flyer_id left join flyer_details c on a.flyer_id=c.flyer_id" 
    +" where a.flyer_id=? order by c.flyer_id desc,b.product_id desc",nativeQuery = true)
    List<Map<String,Object>>findFlyerJoinProducts(int flyerId);

    @Modifying
    @Transactional
    @Query(value = "update flyers set default_select=? where store_id=?",nativeQuery = true)
    void updateDefaultFlyerById(int defaultFlag,int storeId);

    @Query(value = "select b.flyer_img_path from flyers a left join flyer_details b on a.flyer_id=b.flyer_id where default_select=? and store_id=? order by b.flyer_details_id desc",nativeQuery=true)
    List<String>findJoinWithDetail(int defaultSelect,int storeId);

}
