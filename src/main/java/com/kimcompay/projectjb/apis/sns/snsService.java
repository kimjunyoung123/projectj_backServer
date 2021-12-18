package com.kimcompay.projectjb.apis.sns;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.user.userService;
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
    private sqsService sqsService;
    @Autowired
    private userService userService;
    
    public JSONObject send(JSONObject jsonObject,HttpSession httpSession) {
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
            throw utillService.makeRuntimeEX("존재하지 않는 인증방법 혹은 수단입니다 입니다","send");
        }
        logger.info("조회할 정보: "+val);
        //db에 전화번호/이메일 찾기 (count 로 가져옴)
        Map<String,Object>dpe=userService.getCount(val);
        String upoe=dpe.get(keys[0]).toString();
        String cpoe=dpe.get(keys[1]).toString();
        logger.info("일반회원: "+upoe+", 기업회원: "+cpoe);
        logger.info(detail);
        //찾기라면 카운트가 1 이여야하고 가입이라면 0이여야함
        if(detail.equals(senums.auth.get())){
            logger.info("가입이 안되어있어야하는요청");
            if(!upoe.equals("0")||!cpoe.equals("0")){
                logger.info("이미 가입 되어있는 정보");
                utillService.throwRuntimeEX("이미 가입되어있는 "+type+"입니다");
            }
        }else if(detail.equals(senums.find.get())){
            logger.info("가입이 되어있어야하는요청");
            if(upoe.equals("0")||cpoe.equals("0")){
                logger.info("회원 정보가 존재하지 않습니다");
                utillService.throwRuntimeEX("가입된 회원정보가 존재하지 않습니다");
            }
        }else{
            logger.info("비회원 요청");
        }
        //세션에 요청정보 담기
        String num=utillService.getRandomNum(len);
        Map<String,Object>map=new HashMap<>();
        map.put("type", type);
        map.put("val", val);
        map.put("num",num);
        map.put("detail",detail);
        httpSession.setMaxInactiveInterval(limiteMin*60);
        httpSession.setAttribute(detail+type, map);
        //이메일/휴대폰 구분전송
        if(type.equals(senums.phonet.get())){
            logger.info("휴대폰 전송시도");
        }else if(type.equals(senums.emailt.get())){
            logger.info("이메일 전송시도");
        }else{
            utillService.throwRuntimeEX("지원하지 않는 인증방식입니다");
        }
        //sqs전송요청
        sqsService.sendSqs("인증번호는 "+num+"입니다", type,val);
        return utillService.getJson(true, "인증번호가 "+type+"로 전송되었습니다");
    }
    public JSONObject confrim(JSONObject jsonObject,HttpSession session) {
        logger.info("confrim");
        //요청정보 표시
        logger.info(jsonObject.toString());
        //세션에서 요청 기록꺼내기
        Map<String,Object>map=new HashMap<>();
        String key=jsonObject.get("detail").toString()+jsonObject.get("type").toString();
        logger.info("조회키: "+key);
        try {
            logger.info(session.getAttribute(key).toString());
            map=(Map<String,Object>)session.getAttribute(key);
        } catch (NullPointerException e) {
            logger.info("인증 요청 내역없음");
            utillService.throwRuntimeEX("요청 기록이 존재하지 않습니다 다시 요청 해주세요");
        }
        logger.info(map.toString());
        //비교하기
        String rnum=Optional.ofNullable(jsonObject.get("val").toString()).orElseThrow(()->utillService.throwRuntimeEX("인증번호를 입력해주세요"));
        utillService.checkBlank(rnum);
        String num=map.get("num").toString();
        if(rnum.equals(num)){
            map.put("res",true);
            map.put(map.get("type").toString(), map.get("val"));
            logger.info("인증후세션 내역: "+map.toString());
            session.setAttribute(key, map);
            return utillService.getJson(true, "인증성공");
        }
        return utillService.getJson(false, "인증번호 불일치");
        
    }
}
