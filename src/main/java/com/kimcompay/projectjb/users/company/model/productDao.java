package com.kimcompay.projectjb.users.company.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface productDao extends JpaRepository<productVo,Integer> {
    
    List<productVo>findByFlyerId(int flyerId);

    @Query(value = "select count(*) from flyers where flyer_id=?",nativeQuery = true)
    int countFlyerByFlyerId(int flyerId);
}
