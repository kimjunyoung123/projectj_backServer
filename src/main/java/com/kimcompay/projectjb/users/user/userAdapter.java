package com.kimcompay.projectjb.users.user;

import java.util.HashMap;
import java.util.Map;

import com.kimcompay.projectjb.users.company.comVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class userAdapter {
    private Logger logger=LoggerFactory.getLogger(userAdapter.class);
    private userVo userVo;
    private comVo comVo;
    private Map<Object,Object>map=new HashMap<>();
    private boolean flag=false;

    public void adapterCom(comVo comVo){
        logger.info("adapterCom");
        this.comVo=comVo;
        this.flag=false;
    };
    public void adapterUser(userVo userVo){
        logger.info("adapterUser");
        this.userVo=userVo;
        this.flag=true;
    }
    public Map<Object,Object> getMap() {
        logger.info("getMap");
        map.clear();
        if(flag){
            logger.info("user to map");
            map.put("email", userVo.getEmail());
            map.put("address", userVo.getUaddress());
            map.put("created", userVo.getUcreated());
            map.put("detail_address", userVo.getUdetail_address());
            map.put("id", userVo.getUid());
            map.put("login_date", userVo.getUlogin_date());
            map.put("phone", userVo.getUphone());
            map.put("post_code", userVo.getUpostcode());
            map.put("pwd", userVo.getUpwd());
            map.put("role", userVo.getUrole());
            map.put("sleep", userVo.getUsleep());
        }else{
            logger.info("com to map");
            map.put("email", comVo.getCemail());
            map.put("address", comVo.getCaddress());
            map.put("created", comVo.getCcreated());
            map.put("detail_address", comVo.getCdetail_address());
            map.put("id", comVo.getCid());
            map.put("login_date", comVo.getClogin_date());
            map.put("phone", comVo.getCphone());
            map.put("post_code", comVo.getCpostcode());
            map.put("pwd", comVo.getCpwd());
            map.put("role", comVo.getCrole());
            map.put("sleep", comVo.getCsleep());
            map.put("kind", comVo.getCkind());
            map.put("num", comVo.getCnum());
            map.put("tel", comVo.getCtel());
            map.put("start_time", comVo.getStart_time());
            map.put("close_time", comVo.getClose_time());
        }
        return map;
    }
}
