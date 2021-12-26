package com.kimcompay.projectjb.apis.naver;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.user.userService;
import com.kimcompay.projectjb.users.user.userVo;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class naverLoginService {
    private Logger logger=LoggerFactory.getLogger(naverLoginService.class);
    
    @Value("${oauth.pwd}")
    private String oauthPwd;
    @Value("${access_token_cookie}")
    private String accessTokenCookieName;
    @Value("${refresh_token_cookie}")
    private String refreshTokenCookieName;
    @Autowired
    private requestTo requestTo;
    @Autowired
    private userService userService;
    @Autowired
    private jwtService jwtService;


    public JSONObject login(String clientId,String ClientPwd,String code,String state) {
        logger.info("login");
        //토큰발급
        //토큰 꺼내기
        JSONObject result=new JSONObject();
        String accessToken=null;
        try {
            result=getToken(code, clientId,ClientPwd, state);
            logger.info("네이버 통신결과: "+result);
            accessToken=result.get("access_token").toString();
            logger.info("엑세스토큰: "+accessToken);
        } catch (NullPointerException |HttpClientErrorException e) {
            logger.info("네이버 통신에러 발생: "+e.getMessage());
            return utillService.getJson(false, "네이버 통신에 실패했습니다");
        }
        //사용자 정보 가져오기
        result=getUserProfile(accessToken);
        logger.info("네이버 통신결과: "+result);
        //네이버 주소 찾아오기
        JSONObject address=getUserPayAddress(accessToken);
        logger.info("네이버 통신결과: "+address);
        //profile꺼내기
        LinkedHashMap<String,Object> profile=(LinkedHashMap<String,Object>)result.get("response");
        //map->vo
        String email=profile.get("email").toString();
        userVo userVo=jsonToVo(profile);
        //로그인처리
        accessToken=jwtService.get_access_token(email);
        String refreshToken=jwtService.get_refresh_token();
        try {
            userService.oauthLogin(userVo,refreshToken);
        } catch (RuntimeException e) {
            logger.info("소셜로그인 에러 발생: "+e.getMessage());
            String message=e.getMessage();
            if(!message.startsWith("메")){
                message=senums.defaultFailMessage.get();
            }
            return utillService.getJson(false, message);
        }
        //쿠키발급
        Map<String,Object>cookies=new HashMap<>();
        cookies.put(accessTokenCookieName, accessToken);
        cookies.put(refreshTokenCookieName, refreshToken);
        utillService.makeCookie(cookies, utillService.getHttpSerResponse());
        return utillService.getJson(true, "네이버 로그인 완료");
    }
    private userVo jsonToVo(LinkedHashMap<String,Object> profile) {
        logger.info("jsonToVo");
        userVo vo=userVo.builder().email(profile.get("email").toString()).uaddress("테스트주소없음").ubirth(profile.get("birthyear")+"-"+profile.get("birthday"))
                        .udetail_address("테스트주소안줌").ulogin_date(Timestamp.valueOf(LocalDateTime.now())).uphone(profile.get("mobile").toString()).upostcode("테스트안줌")
                        .upwd(oauthPwd).urole(senums.user_role.get()).usleep(0).build();
                        return vo;
    }
    private JSONObject getToken(String code,String clientId,String clientPwd,String state) {
        logger.info("getToken");
        return requestTo.requestGet(null,"https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id="+clientId+"&client_secret="+clientPwd+"&code="+code+"&state="+state+"", null);
    }
    private JSONObject getUserProfile(String accessToken) {
        logger.info("getUserProfile");
        HttpHeaders headers=new HttpHeaders();
        headers.add("Authorization", "Bearer "+accessToken);
        return requestTo.requestGet(null, "https://openapi.naver.com/v1/nid/me", headers);
    }
    private JSONObject getUserPayAddress(String accessToken) {
        logger.info("getUserPayAddress");
        HttpHeaders headers=new HttpHeaders();
        headers.add("Authorization", "Bearer "+accessToken);
        return requestTo.requestGet(null, "https://openapi.naver.com/v1/naverpay/address", headers);
    }
}
