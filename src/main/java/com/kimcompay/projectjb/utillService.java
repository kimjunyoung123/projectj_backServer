package com.kimcompay.projectjb;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;

public class utillService {
    private final static Logger logger=LoggerFactory.getLogger(utillService.class);

    public static JSONObject getJson(boolean flag,String message) {
        logger.info("getjosn");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("flag", flag);
        jsonObject.put("message", message);
        return jsonObject;
    }
    public static RuntimeException makeRuntimeEX(String message,String methodName) {
        logger.info("getRuntimeEX");
        logger.info(methodName);
        return new RuntimeException("메세지: "+message);
    }
    public static RuntimeException throwRuntimeEX(String message) {
        logger.info("throwRuntimeEX");
        throw new RuntimeException("메세지: "+message);
    }
    public static boolean checkBlank(String ob) {
        logger.info("checkBlank");
        if(ob.isBlank()){
            return true;
        }
        return false;
    }
    public static String getRandomNum(int len) {
        String num="";
        Random random=new Random();
        for(int i=0;i<len;i++){
            num+=Integer.toString(random.nextInt(10));
        }
        return num;
    } 
    public static <T> T getValue(T ob,String error_message,String method_name) {
        logger.info("getValue");
        return Optional.ofNullable(ob).orElseThrow(()->makeRuntimeEX(error_message,method_name));
    }
    public static void makeCookie(Map<String,Object>infor,HttpServletResponse response) {
        logger.info("makeCookie");
        logger.info("쿠키내용: "+infor.toString());
        for(Entry<String, Object> key:infor.entrySet()){
            ResponseCookie cookie = ResponseCookie.from(key.getKey(),key.getValue().toString()) 
            .sameSite("None") 
            .secure(true) 
            .path("/") 
            .build(); 
            response.addHeader("Set-Cookie", cookie.toString()+";HttpOnly");  
        }
    }
    public static String getCookieValue(HttpServletRequest request,String cookieName) {
        logger.info("getCookieValue");
        Cookie[] cookies=request.getCookies();
        for(Cookie c:cookies){
            if(c.getName().equals(cookieName)){
                return c.getValue();
            }
        }
        return null;
    }
    public static void doRedirect(HttpServletResponse response,String url) {
        logger.info("doRedirect");
        logger.info(url+"리다이렉트 요청 url");
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("doRedirect error"+e.getMessage());
        }
    }

}
