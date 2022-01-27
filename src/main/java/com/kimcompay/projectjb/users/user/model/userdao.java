package com.kimcompay.projectjb.users.user.model;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface userdao extends JpaRepository<userVo,Integer>{
    
    @Query(value = "select (select count(*) from users where user_phone=?)up,(select count(*) from companys where company_phone=?)cp,(select count(*) from users where user_email=?)ue,(select count(*) from companys where company_email=?)ce ",nativeQuery = true)
    Map<String,Object> findByPhoneAndEmailJoinCompany(String eorp,String sam_eorp,String sam_eorp2,String sam_eorp3);

    @Query(value = "select count(*) from companys where company_num=?",nativeQuery = true)
    int countByCnumNative(Long company_num);

    Optional<userVo>findByEmail(String email);

    @Query(value = "select (select user_email from users where user_phone=?)ue,(select company_email from companys where company_phone=?)ce",nativeQuery = true)
    Map<String,Object>findEmailByPhone(String phone,String samPhone);

    @Query(value = "select (select user_phone from users where user_email=?)up,(select company_phone from companys where company_email=?)cp",nativeQuery = true)
    Map<String,Object>findPhoneByEmail(String email,String sameEmail);

    @Query(value = "select (select count(*) from users where user_email=?)uc,(select count(*) from companys where company_email=?)cc",nativeQuery = true)
    Map<String,Object>countByEmail(String email,String sam_email);

    @Modifying
    @Transactional
    @Query(value = "update users set user_pwd=? where user_email=?",nativeQuery = true)
    void updateUserPwd(String hashPwd,String email);

    @Modifying
    @Transactional
    @Query(value = "update companys set company_pwd=? where company_email=?",nativeQuery = true)
    void updateCompanyPwd(String hashPwd,String email);

    
    @Modifying
    @Transactional
    @Query(value = "update users set user_login_date=? where user_email=?",nativeQuery = true)
    void updateUserLoginDate(Timestamp loginDate,String email);

    @Modifying
    @Transactional
    @Query(value = "update companys set company_login_date=? where company_email=?",nativeQuery = true)
    void updateCompanyLoginDate(Timestamp loginDate,String email);

    userVo findByUphone(String phone);
}
