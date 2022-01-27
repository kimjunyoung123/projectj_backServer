package com.kimcompay.projectjb.users.company.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface storeDao extends JpaRepository<storeVo,Integer> {
    
    @Query(value = "select count(*) from companys where cnum=?",nativeQuery = true)
    int countBySnum(Long snum);

    @Query(value = "select count(*) from stores where store_name=? and store_num=? and store_address=?",nativeQuery = true)
    int countBySnameAndAddress(String storeName,String companyNum,String address);

    @Query(value = "select store_name,thumb_nail,store_address,store_sleep,sid,(select count(*) from stores where cid=?)totalCount from stores where cid=? order by sid desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreNameNokeyword(int cid,int sameCid,int start,int pageSize);

    @Query(value = "select store_name,thumb_nail,store_address,store_sleep,sid,(select count(*) from stores where cid=? and store_name like %?%)totalCount from stores where cid=? and store_name like %?% order by sid desc limit ?,?",nativeQuery = true)
    List<Map<String,Object>> findByStoreInKeyword(int cid,String keyword,int sameCid,String sameKeyword,int start,int pageSize);
    
    Optional<storeVo> findBySid(Object id);

}
