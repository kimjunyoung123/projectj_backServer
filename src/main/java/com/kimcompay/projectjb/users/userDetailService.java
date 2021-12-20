package com.kimcompay.projectjb.users;

import java.util.HashMap;
import java.util.Map;

import com.kimcompay.projectjb.users.company.comVo;
import com.kimcompay.projectjb.users.company.compayDao;
import com.kimcompay.projectjb.users.user.userVo;
import com.kimcompay.projectjb.users.user.userdao;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("loadUserByUsername");
        Map<String,Object>princi=new HashMap<>();
        userVo userVo=userdao.findByEmail(username).orElseGet(()->null);
        if(userVo==null){
            logger.info("기업회원인지 찾기");
            comVo comVo=compayDao.findByCemail(username).orElseThrow(()-> new UsernameNotFoundException("회원정보를 찾을 수없습니다"));
            princi.put("dto", comVo);
            princi.put("sleep", comVo.getCsleep());
            princi.put("email", comVo.getCemail());
            princi.put("pwd", comVo.getCpwd());
            princi.put("role", comVo.getCrole());
            comVo.setCpwd(null);
        }else{
            princi.put("dto", userVo);
            princi.put("sleep", userVo .getUsleep());
            princi.put("email", userVo .getEmail());
            princi.put("pwd", userVo .getUpwd());
            princi.put("role", userVo .getUrole());
            userVo.setUpwd(null);
        }
        return new principalDetails(princi);
    }
}
