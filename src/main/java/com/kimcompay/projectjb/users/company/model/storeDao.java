package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countBySnum(Long snum);

    @Query(value = "select count(*) from stores where sname=? and snum=? and saddress=?",nativeQuery = true)
    int countBySnameAndAddress(String storeName,String companyNum,String address);

    @Query(value = "select sname,simg,saddress,ssleep,sid,(select count(*) from stores where semail=?)totalCount from stores where semail=? order by sid desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreNameNokeyword(Object email,Object sameEmail,Object start,Object pageSize);

    @Query(value = "select sname,simg,saddress,ssleep,sid,(select count(*) from stores where semail=? and sname like %?%)totalCount from stores where semail=? and sname like %?% order by sid desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreInKeyword(Object email,Object keyword,Object sameEmail,Object sameKeyword,Object start,Object pageSize);
    
    Optional<storeVo> findBySid(Object id);

}
