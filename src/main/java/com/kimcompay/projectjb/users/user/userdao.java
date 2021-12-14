package com.kimcompay.projectjb.users.user;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface userdao extends JpaRepository<userVo,Integer>{
    
    @Query(value = "select count(*)up,(select count(*) from companys where cphone=?)cp from users where uphone=?",nativeQuery = true)
    Map<String,Object> findByPhoneJoinCompany(String phone,String sam_phone);
}
