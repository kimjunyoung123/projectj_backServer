package com.kimcompay.projectjb.users.company.model.stores;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where company_num=?",nativeQuery = true)
    int countBySnum(Long snum);

    @Query(value = "select count(*) from stores where store_name=? and store_num=? and store_address=?",nativeQuery = true)
    int countBySnameAndAddress(String storeName,String companyNum,String address);

    @Query(value = "select store_name,thumb_nail,store_address,store_sleep,store_id,(select count(*) from stores where company_id=?)totalCount from stores where company_id=? order by store_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreNameNokeyword(int companyId,int sameCompany_id,int start,int pageSize);

    @Query(value = "select store_name,thumb_nail,store_address,store_sleep,store_id,(select count(*) from stores where company_id=? and store_name like %?%)totalCount from stores where company_id=? and store_name like %?% order by store_id desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreInKeyword(int companyId,String keyword,int sameCompany_id,String sameKeyword,int start,int pageSize);
    
}
