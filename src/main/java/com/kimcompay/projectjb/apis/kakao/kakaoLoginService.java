package com.kimcompay.projectjb.apis.kakao;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class kakaoLoginService {
    private Logger logger=LoggerFactory.getLogger(kakaoLoginService.class);
    
    @Autowired
    private requestTo requestTo;
    public void doLogin(String code,String restKey,String redirectUrl) {
        logger.info("doLogin");
        //토큰얻어오기
        JSONObject kTokens=new JSONObject();
        try {
            kTokens=getToken(code,restKey,redirectUrl);
            logger.info("카카오응답: "+kTokens);
        } catch (HttpClientErrorException e) {
            logger.info("카카오 로그인 통신에러 발생");
            logger.info("카카오 에러 메세지: "+e.getMessage());
            //json으로 변환위해 문자열 재수정
            String message=e.getMessage().split(": ")[1];
            message=message.substring(1,message.length()-1);
            JSONObject error=utillService.stringToJson(message);
            logger.info("에러내용: "+error);
            if(error.get("error_code").equals("KOE320")){
                throw utillService.makeRuntimeEX("중복요청 입니다 다시 시도 바랍니다", "doLogin");
            }
            throw utillService.makeRuntimeEX("알 수 없는 에러 발생", "doLogin"); 
        }
        //사용자정보 얻어오기
        

    }
    private JSONObject getToken(String code,String restKey,String redirectUrl) {
        logger.info("getToken");
        logger.info("code: "+code);
        //body만들기
        MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
        multiValueBody.add("grant_type", "authorization_code");//카카오에서  요청하는 고정값
        multiValueBody.add("client_id", restKey);
        multiValueBody.add("redirect_uri", redirectUrl);
        multiValueBody.add("code", code);
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return requestTo.requestPost(multiValueBody, "https://kauth.kakao.com/oauth/token", headers);
    }
    
}
