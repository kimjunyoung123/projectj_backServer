package com.kimcompay.projectjb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class utillService {
    private final static Logger logger=LoggerFactory.getLogger(utillService.class);

    public static void deleteCookie(String cookieName,HttpServletRequest request,HttpServletResponse response) {
        logger.info("deleteCookie");
        logger.info("제걸될 쿠키이름: "+cookieName);
        ResponseCookie cookie = ResponseCookie.from(cookieName, null) 
        .sameSite("None") 
        .secure(true) 
        .path("/") 
        .maxAge(0)
        .build(); 
        response.addHeader("Set-Cookie", cookie.toString()+";HttpOnly");
    }
    public static JSONObject getJson(boolean flag,String message) {
        logger.info("getjosn");
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("flag", flag);
        jsonObject.put("message", message);
        logger.info("리스폰 담기완료");
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
    public static void goFoward(String errorUrl,HttpServletRequest request,HttpServletResponse response) {
       logger.info("goFoward");
        RequestDispatcher dp=request.getRequestDispatcher(errorUrl);
        try {
            dp.forward(request, response);
        } catch (ServletException | IOException e) {
           logger.info("에러링크 존재 하지 않음");
            e.printStackTrace();
        } 
    }
    public static boolean checkOnlyNum(String snum) {
        logger.info("checkOnlyNum");
        try {
            Integer.parseInt(snum);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
    public static JSONObject stringToJson(String jsonString) {
        logger.info("stringToJson");
        logger.info("json으로 파싱할 문자열: "+jsonString);
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(jsonString);
            JSONObject jsonObj = (JSONObject) obj;
            logger.info("변환: "+jsonObj);
            return jsonObj;
        } catch (ParseException e) {
            logger.info("string to json fail");
            e.printStackTrace();
            throw makeRuntimeEX(senums.defaultFailMessage.get(), "stringToJson");
        }
    }
    public static <T> Boolean checkEmthy(Map<T,T>map) {
        logger.info("checkEmthy");
        if(map.isEmpty()||map.size()==0){
            return true;
        }
        return false;
    }
    public static HttpServletResponse getHttpSerResponse() {
        logger.info("getHttpSerResponse");
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return attr.getResponse();
    }
    public static void makeAllToString(Map<Object,Object>result) {
        logger.info("makeAllToString");
        for(Entry<Object, Object> s:result.entrySet()){
            logger.info(s.getKey().toString());
            if(Optional.ofNullable(s.getValue()).orElseGet(()->null)==null){
                logger.info("null발견 무시");
                continue;
            }else if(!s.getValue().getClass().getSimpleName().equals("String")){
                result.put(s.getKey(), s.getValue().toString());
            }
        }
    }
    public static void makeLoginCookie(String accessToken,String refreshToken,String accessTokenCookieName,String refreshTokenCookieName) {
        logger.info("makeLoginCookie");
        Map<String,Object>cookies=new HashMap<>();
        cookies.put(accessTokenCookieName, accessToken);
        cookies.put(refreshTokenCookieName, refreshToken);
        makeCookie(cookies,getHttpSerResponse());
    }
}
