package com.kimcompay.projectjb.users.user;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface userdao extends JpaRepository<userVo,Integer>{
    
    @Query(value = "select (select count(*) from users where uphone=?)up,(select count(*) from companys where cphone=?)cp,(select count(*) from users where uemail=?)ue,(select count(*) from companys where cemail=?)ce ",nativeQuery = true)
    Map<String,Object> findByPhoneAndEmailJoinCompany(String eorp,String sam_eorp,String sam_eorp2,String sam_eorp3);

    @Query(value = "select (select count(*) from companys where cnum=?)cn,(select count(*) from companys where ctel=?)ct",nativeQuery = true)
    Map<String,Object> countByCnumNative(int company_num,String tel);

    Optional<userVo>findByEmail(String email);

    @Query(value = "select (select uemail from users where uphone=?)ue,(select cemail from companys where cphone=?)ce",nativeQuery = true)
    Map<String,Object>findEmailByPhone(String phone,String samPhone);

    @Query(value = "select (select uphone from users where uemail=?)up,(select cphone from companys where cemail=?)cp",nativeQuery = true)
    Map<String,Object>findPhoneByEmail(String email,String sameEmail);

    @Query(value = "select (select count(*) from users where uemail=?)uc,(select count(*) from companys where cemail=?)cc",nativeQuery = true)
    Map<String,Object>countByEmail(String email,String sam_email);

    @Modifying
    @Transactional
    @Query(value = "update users set upwd=? where uemail=?",nativeQuery = true)
    void updateUserPwd(String hashPwd,String email);

    @Modifying
    @Transactional
    @Query(value = "update companys set cpwd=? where cemail=?",nativeQuery = true)
    void updateCompanyPwd(String hashPwd,String email);
}
