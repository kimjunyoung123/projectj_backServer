package com.kimcompay.projectjb.users.company;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface compayDao extends JpaRepository<comVo,Integer> {
    
    Optional<comVo>findByCemail(String email);
}
