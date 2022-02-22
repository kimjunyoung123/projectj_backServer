package com.kimcompay.projectjb.users.company.model.products;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface productEventDao extends JpaRepository<productEventVo,Integer>{
    List<productEventVo> findByProductId(int productId);

    @Modifying
    @Transactional
    @Query(value = "delete  from product_events where product_id=?",nativeQuery = true)
    void deleteEventsByProductId(int productId);
}
