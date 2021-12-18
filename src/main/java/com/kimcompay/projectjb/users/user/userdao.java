package com.kimcompay.projectjb.users.user;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface userdao extends JpaRepository<userVo,Integer>{
    
    @Query(value = "select (select count(*) from users where uphone=?)up,(select count(*) from companys where cphone=?)cp,(select count(*) from users where uemail=?)ue,(select count(*) from companys where cemail=?)ce ",nativeQuery = true)
    Map<String,Object> findByPhoneAndEmailJoinCompany(String eorp,String sam_eorp,String sam_eorp2,String sam_eorp3);

    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countByCnumNative(int company_num);

    Optional<userVo>findByEmail(String email);
}
