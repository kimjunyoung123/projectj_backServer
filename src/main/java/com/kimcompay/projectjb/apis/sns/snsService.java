package com.kimcompay.projectjb.apis.sns;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.apis.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.user.userdao;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class snsService {
    
    private Logger logger=LoggerFactory.getLogger(snsService.class);
    private final int limiteMin=3;
    private final int len=10;

    @Autowired
    private requestTo requestTo;
    @Autowired
    private userdao userdao;
    
    public void send(JSONObject jsonObject,HttpSession httpSession) {
        logger.info("send"+jsonObject.toString());
        //입력값 검사
        String val= Optional.ofNullable(jsonObject.get("val").toString()).orElseThrow(()-> utillService.makeRuntimeEX("이메일/전화번호를 확인해주세요", "send"));
        if(utillService.checkBlank(val)){
            utillService.throwRuntimeEX("이메일/전화번호가 공백일 수없습니다");
        }
        //사용용도 확인
        String detail=jsonObject.get("detail").toString();
        String type=jsonObject.get("type").toString();
        //사용용도/인증수단 확인 if을 줄이려고 enum으로 대체
        String[] keys=new String[2];
        try {
            keys=senums.valueOf(type).get().split(",");
        } catch (IllegalArgumentException e) {
            utillService.throwRuntimeEX("존재하지 않는 인증방법 혹은 수단입니다 입니다");
        }
        logger.info("조회할 정보: "+val);
        //db에 전화번호/이메일 찾기 (count 로 가져옴)
        Map<String,Object>dpe=userdao.findByPhoneAndEmailJoinCompany(val,val,val,val);
        String upoe=dpe.get(keys[0]).toString();
        String cpoe=dpe.get(keys[1]).toString();
        logger.info("일반회원: "+upoe+", 기업회원: "+cpoe);
        logger.info(detail);
        //찾기라면 카운트가 1 이여야하고 가입이라면 0이여야함
        if(detail.equals(senums.auth.get())||detail.equals(senums.find.get())){
            logger.info("가입 되있는지 여부 검사해야하는 요청");
            if(!upoe.equals("0")||!cpoe.equals("0")&&detail.equals(senums.auth.get())){
                logger.info("이미 가입 되어있는 정보");
                utillService.throwRuntimeEX("이미 가입되어있는 "+type+"입니다");
            }else if(upoe.equals("0")||cpoe.equals("0")&&detail.equals(senums.find.get())){
                logger.info("회원 정보가 존재하지 않습니다");
                utillService.throwRuntimeEX("가입된 회원정보가 존재하지 않습니다");
            }
        }else{
            logger.info("비회원 요청");
        }
        //세션에 요청정보 담기
        String num=utillService.getRandomNum(len);
        Map<String,Object>map=new HashMap<>();
        map.put("type", jsonObject.get("type"));
        map.put("val", val);
        map.put("expireDate",LocalDateTime.now().plusMinutes(limiteMin));
        map.put("num",num);
        httpSession.setAttribute(jsonObject.get("detail").toString(), map);
        //이메일/휴대폰 구분전송
        if(type.equals(senums.phonet.get())){
            logger.info("휴대폰 전송시도");
        }else if(type.equals(senums.emailt.get())){
            logger.info("이메일 전송시도");
        }else{
            utillService.throwRuntimeEX("지원하지 않는 인증방식입니다");
        }
        
    }
}
