package com.kimcompay.projectjb.delivery.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface deliverRoomDetailDao extends JpaRepository<deliverRoomDetailVo,Integer>{
    
    @Query(value = "select *from deliver_room_details where room_id=? and done_flag=? and user_id=?",nativeQuery = true)
    Optional<deliverRoomDetailVo> findByFlagAndUserId(int roomId,int flag,int userId);

    @Query(value = "select room_id from deliver_room_details where user_id=? and done_flag=?",nativeQuery = true)
    List<Integer>findAllByRoomIdAndDoneFlag(int userId,int doneFlag);

    @Query(value = "select deliver_room_detail_address,deliver_room_detail_mcht_trd_no,user_id from deliver_room_details where room_id=?",nativeQuery = true)
    List<Map<String,Object>>findAddressByRoomId(int roomId);

    int countByMchtTrdNo(String mchtTrdNo);

    @Query(value = "select a.* from deliver_room_details a inner join delivery_rooms b on a.room_id=? and b.store_id=? and a.deliver_room_detail_mcht_trd_no=? and a.user_id=?",nativeQuery = true)
    Optional<deliverRoomDetailVo> findDeliverByOther(int roomId,int storeId,String mchtTrdNo,int userId);

    
}
