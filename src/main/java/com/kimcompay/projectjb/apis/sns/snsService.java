package com.kimcompay.projectjb.apis.sns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.apis.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.user.userVo;
import com.kimcompay.projectjb.users.user.userdao;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class snsService {
    private Logger logger=LoggerFactory.getLogger(snsService.class);

    @Autowired
    private requestTo requestTo;
    @Autowired
    private userdao userdao;
    
    public void send(JSONObject jsonObject,HttpSession httpSession) {
        logger.info("send");
        String val= Optional.ofNullable(jsonObject.get("val").toString()).orElseThrow(()-> utillService.makeRuntimeEX("이메일/전화번호를 확인해주세요", "send"));
        if(utillService.checkBlank(val)){
            utillService.throwRuntimeEX("이메일/전화번호가 공백일 수없습니다");
        }
        String detail=jsonObject.get("detail").toString();
        Map<String,Object>dp=userdao.findByPhoneJoinCompany(val, val);
        int up=Integer.parseInt(dp.get("up").toString());
        int cp=Integer.parseInt(dp.get("cp").toString());
        try {
            int confrim_num=Integer.parseInt(senums.valueOf(detail).get());
        } catch (IllegalArgumentException e) {
            utillService.throwRuntimeEX("존재하지 않는 회원유형 입니다");
        }
 
       
        
     
       
        

        Map<String,Object>map=new HashMap<>();
        map.put("type", jsonObject.get("type"));
        map.put("val", val);
        httpSession.setAttribute(jsonObject.get("detail").toString(), map);

    }
}
