package com.kimcompay.projectjb.users.company.model;

import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countBySnum(Long snum);

    @Query(value = "select count(*) from stores where sname=? and snum=? and saddress=?",nativeQuery = true)
    int countBySnameAndAddress(String storeName,String companyNum,String address);

    @Query(value = "select sname,simg,saddress,(select count(*) from stores where semail=?)totalCount from stores where semail=? order by sid limit ?,?",nativeQuery = true)
    Map<String,Object>findByEmail(Object email,Object sameEmail,Object start,Object pageSize);
}
