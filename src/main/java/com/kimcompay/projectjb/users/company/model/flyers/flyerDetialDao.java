package com.kimcompay.projectjb.users.company.model.flyers;


import org.springframework.data.jpa.repository.JpaRepository;

public interface flyerDetialDao extends JpaRepository<flyerDetailVo,Integer> {
    
    int countByFlyerId(int flyerId);

}
