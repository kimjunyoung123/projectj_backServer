package com.kimcompay.projectjb.apis.naver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.kenum;
import com.kimcompay.projectjb.enums.nenum;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class naverService {
    private Logger logger=LoggerFactory.getLogger(naverService.class);
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.pwd}")
    private String naverClientPwd;
    @Value("${naver.login.callback.url}")
    private String loginCallback;
    @Value("${front.domain}")
    private String frontDomain;
    @Value("${front.result.page}")
    private String resultLink;
    @Autowired
    private naverLoginService naverLoginService;
    
    public JSONObject callPage(String action) {
        logger.info("callPage");
        String url=null;
        try {
            if(action.equals(nenum.loginPage.get())){
                logger.info("로그인페이지요청");
                try {
                    String state = URLEncoder.encode(loginCallback, "UTF-8");
                    return utillService.getJson(true, "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id="+naverClientId+"&redirect_uri="+""+loginCallback+""+"&state="+state+"");
                } catch (UnsupportedEncodingException e1) {
                    throw new RuntimeException("naverLogin 오류 발생");
                } 
            }
            return utillService.getJson(true, url);
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("지원하지 않는 네이버기능 ", "callPage");
        }
    }
    public void catchCallBack(String action,HttpServletRequest request) {
        logger.info("catchCallBack");
        JSONObject result=new JSONObject();
        if(action.equals(kenum.loginPage.get())){
            logger.info("네이버 로그인 콜백");
            result=naverLoginService.login(naverClientId,naverClientPwd,request.getParameter("code"),request.getParameter("state"));
        }else{
            throw utillService.makeRuntimeEX("알 수없는 오류 발생", "catchCallBack");
        }
        logger.info("처리결과"+result);
        String url=frontDomain+resultLink+"?kind=kakao&action="+action+"&result="+result.get("flag")+"&message="+result.get("message");
        //utillService.doRedirect(utillService.getHttpSerResponse(), url);
    }
}
