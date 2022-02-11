package com.kimcompay.projectjb.delivery.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliveryRoomDao extends JpaRepository<deliveryRoomVo,Integer> {


   /* @Query(value = "select room_id from delivery_rooms where company_id=? and deliver_room_flag=? and delivery_rooms_flag",nativeQuery = true)
    List<Integer>findAllByMasterIdAndFlag(int companyId,int doneFlag,int startFlag);*/

    @Query(value = "select *,(select count(*) from delivery_rooms where deliver_room_created between ? and ? and store_id=? and deliver_rooms_flag=?)totalCount from delivery_rooms where deliver_room_created between ? and ? and store_id=? and deliver_rooms_flag=? order by room_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByDay(Timestamp daystart,Timestamp dayEnd,int storeId,int state,Timestamp sameDaystart,Timestamp sameDayEnd,int sameStoreId,int sameState,int page,int pageSize);

    @Query(value = "select *,(select count(*) from delivery_rooms where store_id=? and deliver_rooms_flag=?)totalCount from delivery_rooms where store_id=? and deliver_rooms_flag=? order by room_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>>findByAll(int store_id,int state,int sameStore_id, int sameState,int start,int pageSize);

}
