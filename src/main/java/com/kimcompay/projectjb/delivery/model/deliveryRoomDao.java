package com.kimcompay.projectjb.delivery.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliveryRoomDao extends JpaRepository<deliveryRoomVo,Integer> {


    @Query(value = "select room_id from delivery_rooms where company_id=? and deliver_room_flag=? and start_flag",nativeQuery = true)
    List<Integer>findAllByMasterIdAndFlag(int companyId,int doneFlag,int startFlag);


}
