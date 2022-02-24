package com.kimcompay.projectjb.users.company.model.flyers;


import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface flyerDetialDao extends JpaRepository<flyerDetailVo,Integer> {
    
    int countByFlyerId(int flyerId);

    void deleteByFlyerId(int flyerId);

    @Query(value = "select flyer_img_path from flyer_details where flyer_id=?",nativeQuery = true)
    List<Map<String,Object>> findAllImgPathsByFlyerId(int flyerId);

    List<flyerDetailVo>findByFlyerId(int flyerId);

}
