package com.kimcompay.projectjb.delivery.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliverRoomDetailDao extends JpaRepository<deliverRoomDetailVo,Integer>{
    
    @Query(value = "select *from deliver_room_details where room_id=? and user_id=?",nativeQuery = true)
    deliverRoomDetailVo findByRoomIdAndUserId(Object roomId,Object userId);
}
