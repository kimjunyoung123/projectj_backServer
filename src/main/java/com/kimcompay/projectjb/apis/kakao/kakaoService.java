package com.kimcompay.projectjb.apis.kakao;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.kenum;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class kakaoService {
    private Logger logger=LoggerFactory.getLogger(kakaoService.class);
    
    @Value("${kakao.app.key}")
    private String app_key;
    @Value("${kakao.rest.key}")
    private String rest_key;
    @Value("${kakao.js.key}")
    private String js_key;
    @Value("${kakao.admin.key}")
    private String admin_key;
    @Value("${k_login_callback_url}")
    private String kLoginCallbackUrl;
    @Value("${front.domain}")
    private String frontDomain;

    @Autowired
    private kakaoLoginService kakaoLoginService;

    public JSONObject callPage(String action) {
        logger.info("callPage");
        String url=null;
        if(action.equals(kenum.loginPage.get())){
            logger.info("k로그인요청");
            logger.info("콜백 url: "+kLoginCallbackUrl);
            url="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+rest_key+"&redirect_uri="+kLoginCallbackUrl;
        }else{
            throw utillService.makeRuntimeEX("지원하지 않는 카카오페이지 입니다", "callPage");
        }
        return utillService.getJson(true, url);
    }
    public void catchCallBack(String action,HttpServletRequest request) {
        logger.info("catchCallBack");
        String url=null;
        JSONObject result=new JSONObject();
        if(action.equals(kenum.loginPage.get())){
            logger.info("k로그인 콜백");
            url="social";
            result=kakaoLoginService.doLogin(request.getParameter("code"),rest_key,kLoginCallbackUrl);
        }else{
            throw utillService.makeRuntimeEX("알 수없는 오류 발생", "catchCallBack");
        }
        logger.info("forword");
        url=frontDomain+url+"?result="+result.get("flag")+"&message="+result.get("message");
        utillService.doRedirect(utillService.getHttpSerResponse(), url);
    }
}
