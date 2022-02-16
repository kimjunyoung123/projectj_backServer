package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface productEventDao extends JpaRepository<productEventVo,Integer>{
    List<productEventVo> findByProductId(int productId);
}
