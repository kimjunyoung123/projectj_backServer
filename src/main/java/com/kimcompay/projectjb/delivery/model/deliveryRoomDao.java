package com.kimcompay.projectjb.delivery.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliveryRoomDao extends JpaRepository<deliveryRoomVo,Integer> {
    @Query(value  = "select count(*) from delivery_rooms where deliver_room_master=?",nativeQuery = true)
    int findCountByRoomMaster(Object compayId);



}
