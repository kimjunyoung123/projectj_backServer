package com.kimcompay.projectjb.apis.kakao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.kenum;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.user.userService;
import com.kimcompay.projectjb.users.user.model.userVo;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class kakaoLoginService {
    private Logger logger=LoggerFactory.getLogger(kakaoLoginService.class);

    @Value("${oauth.pwd}")
    private String oauthPwd;
    @Autowired
    private requestTo requestTo;
    @Autowired
    private userService userService;
    @Autowired
    private sqsService sqsService;

    public JSONObject doLogin(String code,String restKey,String redirectUrl) {
        logger.info("doLogin");
        //토큰얻어오기
        JSONObject response=new JSONObject();
        try {
            response=getToken(code,restKey,redirectUrl);
            logger.info("카카오응답: "+response);
        } catch (HttpClientErrorException e) {
            logger.info("카카오 로그인 통신에러 발생");
            logger.info("카카오 에러 메세지: "+e.getMessage());
            //json으로 변환위해 문자열 재수정
            String message=e.getMessage().split(": ")[1];
            message=message.substring(1,message.length()-1);
            JSONObject error=utillService.stringToJson(message);
            logger.info("에러내용: "+error);
            try {
                //if대신 enum사용
                return  utillService.getJson(false,kenum.valueOf(error.get("error_code").toString()).get());
            } catch (IllegalArgumentException e2) {
                return  utillService.getJson(false,senums.defaultFailMessage.get());
            }
        }
        //사용자정보 얻어오기
        response=getKuserInfor(response);
        logger.info("카카오응답: "+response);
        LinkedHashMap<String,Object>linkedHashMap=(LinkedHashMap<String,Object>)response.get("kakao_account");
        logger.info("유저정보: "+linkedHashMap);
        //정보분리
        LinkedHashMap<String,Object>profile=(LinkedHashMap<String,Object>)linkedHashMap.get("profile");
        //테스트계정 주소/생일/휴대전화 받을 수 없음 임의로 만들자 
        profile.put("address", senums.defaultAddress.get());
        profile.put("postCode", senums.defaultPostcode.get());
        profile.put("detailAddress", senums.defaultDetailAddress.get());
        profile.put("phone", "01011113333");
        profile.put("birth", "1996-01-01");
        //map->vo
        String email=linkedHashMap.get("email").toString();
        userVo userVo=mapToVo(profile,email);
        //로그인처리
        try {
            userService.oauthLogin(email, userVo);
        } catch (Exception e) {
            return utillService.getJson(false, e.getMessage());
        }
        sqsService.sendEmailAsync("로그인 알림 이메일입니다 로그인일자: "+userVo.getUlogin_date(), email);
        return utillService.getJson(true, "카카오 로그인 완료");
    }
    private userVo mapToVo(LinkedHashMap<String,Object>profile,String email) {
        logger.info("mapToVo");
        userVo vo=userVo.builder().email(email).uaddress(profile.get("address").toString()).ubirth(profile.get("birth").toString()).udetail_address(profile.get("detailAddress").toString()).ulogin_date(Timestamp.valueOf(LocalDateTime.now()))
                        .uphone(profile.get("phone").toString()).upostcode(profile.get("postCode").toString()).upwd(oauthPwd).urole(senums.user_role.get()).usleep(0).build();
                                    return vo;
    }
    private JSONObject getKuserInfor(JSONObject response) {
        logger.info("getKuserInfor");
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "Bearer "+response.get("access_token"));
        response.clear();
        return requestTo.requestPost(null, "https://kapi.kakao.com/v2/user/me", httpHeaders);

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
