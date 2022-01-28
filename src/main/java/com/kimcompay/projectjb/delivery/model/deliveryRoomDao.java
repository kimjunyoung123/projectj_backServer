package com.kimcompay.projectjb.delivery.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliveryRoomDao extends JpaRepository<deliveryRoomVo,Integer> {


   /* @Query(value = "select room_id from delivery_rooms where company_id=? and deliver_room_flag=? and delivery_rooms_flag",nativeQuery = true)
    List<Integer>findAllByMasterIdAndFlag(int companyId,int doneFlag,int startFlag);*/

    @Query(value = "select * from delivery_rooms where deliver_room_created between ? and ? and store_id=?",nativeQuery = true)
    List<deliveryRoomVo>findByDay(Timestamp daystart,Timestamp dayEnd,int store_id);


}
