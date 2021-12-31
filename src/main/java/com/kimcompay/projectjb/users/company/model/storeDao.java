package com.kimcompay.projectjb.users.company.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countBySnum(int snum);

    @Query(value = "select count(*) from stores where sname=? and snum=? and saddress=?",nativeQuery = true)
    int countBySnameAndAddress(String storeName,String companyNum,String address);
}
