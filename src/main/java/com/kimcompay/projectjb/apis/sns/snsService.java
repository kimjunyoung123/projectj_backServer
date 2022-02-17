package com.kimcompay.projectjb.apis.sns;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.user.userService;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class snsService {
    private final int limiteMin=3;
    private final int len=10;

    @Value("${change.pwd.link}")
    private String changePwdLink;
    @Autowired
    private sqsService sqsService;
    @Autowired
    private userService userService;
    @Autowired
    private jwtService jwtService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    
    public JSONObject send(JSONObject jsonObject,HttpSession httpSession) {
        //입력값 검사
        String val= null;
        //사용용도 확인
        String detail=jsonObject.get("detail").toString();
        String type=jsonObject.get("type").toString();
        try {
            val= jsonObject.get("val").toString().trim();
            if(utillService.checkBlank(val)){
               throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            throw utillService.makeRuntimeEX(senums.valueOf(type+"Null").get(), "send");
        }
        //사용용도/인증수단 확인 if을 줄이려고 enum으로 대체
        String[] keys=new String[2];
        try {
            keys=senums.valueOf(type).get().split(",");
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("지원하지 않는 인증방법 입니다","send");
        }
        //db에 전화번호/이메일 찾기 (count 로 가져옴)
        Map<String,Object>dpe=userService.getCount(val);
        String upoe=dpe.get(keys[0]).toString();
        String cpoe=dpe.get(keys[1]).toString();
        //찾기라면 카운트가 1 이여야하고 가입이라면 0이여야함
        if(detail.equals(senums.auth.get())){
            //이미 가입되었으면 안되는요청
            if(!upoe.equals("0")||!cpoe.equals("0")){
                utillService.throwRuntimeEX("이미 가입되어있는 "+type+"입니다");
            }
        }else if(detail.equals(senums.find.get())){
            //가입이 되어있어야하는 요청
            if(upoe.equals("0")&&cpoe.equals("0")){
                utillService.throwRuntimeEX("가입된 회원정보가 존재하지 않습니다");
            }
        }else{
            utillService.writeLog("비회원요청", snsService.class);
        }
        //세션에 요청정보 담기
        String num=utillService.getRandomNum(len);
        Map<String,Object>map=new HashMap<>();
        map.put("type", type);
        map.put("val", val);
        map.put("num",num);
        map.put("detail",detail);
        httpSession.setMaxInactiveInterval(limiteMin*60*1000);
        httpSession.setAttribute(detail+type, map);
        //이메일/휴대폰 구분전송
        //비밀번호 찾이요청일경우 이메일로 전송
        if(type.equals(senums.pwtt.get())){
            type=senums.emailt.get();
        }
        //휴대폰전송시도
        if(type.equals(senums.phonet.get())){
            if(utillService.checkOnlyNum(val)){
                throw utillService.makeRuntimeEX("전화번호는 숫자만 있어야합니다", "send");
            }
        }else if(type.equals(senums.emailt.get())){
            //이메일전송시도 딱히 검사할것이없음
        }else{
            utillService.throwRuntimeEX("지원하지 않는 인증방식입니다");
        }
        //sqs전송요청
        sqsService.sendSqs("안녕하세요 장보고입니다","인증번호는 "+num+"입니다", type,val);
        return utillService.getJson(true, "인증번호가 "+type+"로 전송되었습니다");
    }
    public JSONObject confrim(JSONObject jsonObject,HttpSession session) {
        //세션에서 요청 기록꺼내기
        Map<String,Object>map=new HashMap<>();
        String key=jsonObject.get("detail").toString()+jsonObject.get("type").toString();
        try {
            map=(Map<String,Object>)session.getAttribute(key);
        } catch (NullPointerException e) {
            e.printStackTrace();
            utillService.writeLog("인증 요청 내역없음",snsService.class);
            utillService.throwRuntimeEX("요청 기록이 존재하지 않습니다 다시 요청 해주세요");
        }
        //비교하기
        String rnum=null;
        try {
            rnum=Optional.ofNullable(jsonObject.get("val").toString()).orElseThrow(()->new NullPointerException());
            if(utillService.checkBlank(rnum)){
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
           throw utillService.makeRuntimeEX("인증번호를 입력해 주세요", "confrim");
        }
        String num=map.get("num").toString().trim();
        if(rnum.equals(num)){
            //용도에 맞춰서 처리
            String message="인증성공";
            if(key.startsWith(senums.find.get())){
                message=doFindAction(map.get("val").toString(), map.get("type").toString());
                session.removeAttribute(key);
            }else{
                map.put("res",true);
                map.put(map.get("type").toString(), map.get("val"));
                session.setAttribute(key, map);
            }
            return utillService.getJson(true, message);
        }
        return utillService.getJson(false, "인증번호 불일치");
        
    }
    private String doFindAction(String val,String type) {
        String res="이메일: ";
        //비밀번호 찾기일경구
        if(type.equals(senums.pwtt.get())){
            String phone=null;
            //요청알림을 위해 휴대폰 번호 조회
            Map<String,Object>phones=userService.selectPhoneByEmail(val);
            for(Entry<String, Object>m:phones.entrySet()){
                if(m.getValue()==null){
                    continue;
                }
                phone=m.getValue().toString();
            }
            //비동기 비밀번호 변경요청알림
            sqsService.sendEmailAsync("비밀번호 변경 요청이 확인되었습니다", val);
            sqsService.sendPhoneAsync("비밀번호 변경 요청이 확인되었습니다", phone);
            //비밀번호요청 링크전송
            String changePwdToken=jwtService.get_refresh_token();
            String cPwdLink=changePwdLink+changePwdToken;
            //redis저장 
            Map<String,Object>map=new HashMap<>();
            map.put("email", val);
            map.put("expire", LocalDateTime.now().plusDays(1).toString());
            redisTemplate.opsForHash().putAll(changePwdToken, map);
            //유효기간
            redisTemplate.expire(changePwdToken,1,TimeUnit.DAYS);
            sqsService.sendSqs("안녕하세요 장보고입니다","비밀번호 변경 링크입니다\n"+cPwdLink+"\n 위링크는 하루동안만 유효합니다",senums.emailt.get(), val);
            res="이메일로 링크가 전송되었습니다";
        }else{
            //이메일찾기일경우
            Map<String,Object>map=userService.selectEmailByPhone(val);
            for(Entry<String, Object>m:map.entrySet()){
                if(m.getValue()==null){
                    continue;
                }
                res+=m.getValue().toString();
            }
        }
        return res;
    }
}
