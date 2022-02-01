package com.kimcompay.projectjb.delivery.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliverRoomDetailDao extends JpaRepository<deliverRoomDetailVo,Integer>{
    
    @Query(value = "select *from deliver_room_details where room_id=? and done_flag=? and user_id=?",nativeQuery = true)
    Optional<deliverRoomDetailVo> findByFlagAndUserId(int roomId,int flag,int userId);

    @Query(value = "select room_id from deliver_room_details where user_id=? and done_flag=?",nativeQuery = true)
    List<Integer>findAllByRoomIdAndDoneFlag(int userId,int doneFlag);

    @Query(value = "select deliver_room_details_address from deliver_room_details where room_id=?",nativeQuery = true)
    List<String>findAddressByRoomId(int roomId);
}
