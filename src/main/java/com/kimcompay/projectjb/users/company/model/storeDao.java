package com.kimcompay.projectjb.users.company.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countBySnum(Long snum);

    @Query(value = "select count(*) from stores where snum=? and saddress=?",nativeQuery = true)
    int countBySnameAndAddress(String companyNum,String address);
}
