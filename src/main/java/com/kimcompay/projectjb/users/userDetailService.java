package com.kimcompay.projectjb.users;

import java.util.HashMap;
import java.util.Map;

import com.kimcompay.projectjb.users.company.model.comVo;
import com.kimcompay.projectjb.users.company.model.compayDao;
import com.kimcompay.projectjb.users.user.model.userAdapter;
import com.kimcompay.projectjb.users.user.model.userVo;
import com.kimcompay.projectjb.users.user.model.userdao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class userDetailService implements UserDetailsService {
    private Logger logger=LoggerFactory.getLogger(userDetailService.class);
    
    @Autowired
    private userdao userdao;
    @Autowired
    private compayDao compayDao;
    @Autowired
    private userAdapter userAdapter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("loadUserByUsername");
        //유저 정보조회
        userVo userVo=userdao.findByEmail(username).orElseGet(()->null);
        Map<Object,Object>princi=new HashMap<>();
        if(userVo==null){
            logger.info("기업회원인지 찾기");
            comVo comVo=compayDao.findByCemail(username).orElseThrow(()-> new UsernameNotFoundException("회원정보를 찾을 수없습니다"));
            userAdapter.adapterCom(comVo);
            princi=userAdapter.getMap();
        }else{
            userAdapter.adapterUser(userVo);
            princi=userAdapter.getMap();
        }
        return new principalDetails(princi);
    }
}
