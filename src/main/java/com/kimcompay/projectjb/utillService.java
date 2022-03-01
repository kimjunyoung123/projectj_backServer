package com.kimcompay.projectjb;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.principalDetails;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class utillService {

    public static String getSettleText(String mchtid,String method,String mchtTrdNo,String requestDate,String requestTime,String totalPrice)  {
        return  String.format("%s%s%s%s%s%s%s",mchtid,method,mchtTrdNo,requestDate,requestTime,totalPrice,senums.settleKey.get());
    }
    public  static void checkOwner(int ucId,String errorMessage) {
        if(ucId!=utillService.getLoginId()){
            throw utillService.makeRuntimeEX(errorMessage, "checkOwner");
        }
    }
    public static List<String> getImgSrc(String text) {
    	List<String>array=new ArrayList<>();
        try {
            Pattern nonValidPattern = Pattern.compile("<img[^>]*src=[\"']?([^>\"']+)[\"']?[^>]*>");
            Matcher matcher = nonValidPattern.matcher(text);
            while (matcher.find()) {
                array.add(matcher.group(1));
            }
        } catch (NullPointerException e) {
            utillService.writeLog("삭제할 사진을 찾지 못했습니다", utillService.class);
        }
        return array;
    }
    public static void deleteCookie(String cookieName,HttpServletRequest request,HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, null) 
        .sameSite("None") 
        .secure(true) 
        .path("/") 
        .maxAge(0)
        .build(); 
        response.addHeader("Set-Cookie", cookie.toString()+";HttpOnly");
    }
    public static JSONObject getJson(boolean flag,Object message) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("flag", flag);
        jsonObject.put("message", message);
        return jsonObject;
    }
    public static RuntimeException makeRuntimeEX(String message,String methodName) {
        writeLog(message, utillService.class);
        return new RuntimeException("메세지: "+message);
    }
    public static RuntimeException throwRuntimeEX(String message) {
        throw new RuntimeException("메세지: "+message);
    }
    public static boolean checkBlank(String ob) {
        if(ob==null){
            return true;
        }else if(ob.equals("null")||ob.equals("undifined")){
            return true;
        }
        else if(ob.isBlank()){
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
        return Optional.ofNullable(ob).orElseThrow(()->makeRuntimeEX(error_message,method_name));
    }
    public static void makeCookie(Map<String,Object>infor,HttpServletResponse response) {
        for(Entry<String, Object> key:infor.entrySet()){
           /* ResponseCookie cookie = ResponseCookie.from(key.getKey(),key.getValue().toString()) 
            .sameSite("None") 
            .secure(true)
            .path("/")
            .build(); 
            response.addHeader("Set-Cookie", cookie.toString()+";HttpOnly");  */
            Cookie c=new Cookie(key.getKey(), key.getValue().toString());
            response.addCookie(c); //테스트용
        }
    }
    public static String getCookieValue(HttpServletRequest request,String cookieName) {
        Cookie[] cookies=request.getCookies();
        for(Cookie c:cookies){
            if(c.getName().equals(cookieName)){
                return c.getValue();
            }
        }
        return null;
    }
    public static void doRedirect(HttpServletResponse response,String url) {
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
            writeLog("doRedirect error"+e.getMessage(),utillService.class);
        }
    }
    public static void goFoward(String errorUrl,HttpServletRequest request,HttpServletResponse response) {
        RequestDispatcher dp=request.getRequestDispatcher(errorUrl);
        try {
            dp.forward(request, response);
        } catch (ServletException | IOException e) {
            writeLog("에러링크 존재 하지 않음",utillService.class);
            e.printStackTrace();
        } 
    }
    public static boolean checkOnlyNum(String snum) {
        try {
            Long.parseLong(snum);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
    public static JSONObject stringToJson(String jsonString) {
        JSONParser parser = new JSONParser();
        Object obj;
        try {
            obj = parser.parse(jsonString);
            JSONObject jsonObj = (JSONObject) obj;
            return jsonObj;
        } catch (ParseException e) {
            e.printStackTrace();
            throw makeRuntimeEX(senums.defaultFailMessage.get(), "stringToJson");
        }
    }
    public static <T> Boolean checkEmthy(Map<T,T>map) {
        if(map.isEmpty()||map.size()==0){
            return true;
        }
        return false;
    }
    public static <T> boolean checkEmthy(List<T>arr) {
        if(arr==null){
            return true;
        }
        if(arr.size()==0||arr.isEmpty()){
            return true;
        }
        return false;
    }
    public static HttpServletResponse getHttpSerResponse() {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return attr.getResponse();
    }
    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        return attr.getRequest();
    }
    public static void makeAllToString(Map<Object,Object>result) {
        for(Entry<Object, Object> s:result.entrySet()){
            if(Optional.ofNullable(s.getValue()).orElseGet(()->null)==null){
                continue;
            }else if(!s.getValue().getClass().getSimpleName().equals("String")){
                result.put(s.getKey(), s.getValue().toString());
            }
        }
    }
    public static void makeLoginCookie(String accessToken,String refreshToken,String accessTokenCookieName,String refreshTokenCookieName) {
        Map<String,Object>cookies=new HashMap<>();
        cookies.put(accessTokenCookieName, accessToken);
        cookies.put(refreshTokenCookieName, refreshToken);
        makeCookie(cookies,getHttpSerResponse());
    }
    public static String checkAuthPhone(String authText) {
        Map<String,Object>authInfor=new HashMap<>();
        boolean checkFlag=false;
        String sphone=null;
        //인증정보꺼내기
        try {
            HttpSession httpSession=getHttpServletRequest().getSession();
            authInfor=(Map<String,Object>)httpSession.getAttribute(authText+senums.phonet.get());
            sphone=authInfor.get("val").toString();
            checkFlag=Boolean.parseBoolean(authInfor.get("res").toString());
        } catch (NullPointerException e) {
            throw utillService.makeRuntimeEX("휴대폰 인증을 먼저 해주세요", "checkAuth");
        }
        if(!checkFlag){
            throw utillService.makeRuntimeEX("휴대폰 인증이 되지 않았습니다", "checkAuth");
        }
        return sphone;
    }
    public static String checkAuthEmail() {
        Map<String,Object>authInfor=new HashMap<>();
        boolean checkFlag=false;
        String sEmail=null;
        try {
            HttpSession httpSession=getHttpServletRequest().getSession();
            authInfor=(Map<String,Object>)httpSession.getAttribute(senums.auth.get()+senums.emailt.get());
            sEmail=authInfor.get("val").toString();
            checkFlag=Boolean.parseBoolean(authInfor.get("res").toString());
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("이메일 인증을 먼저 해주세요", "checkAuth");
        }
        if(!checkFlag){
            throw utillService.makeRuntimeEX("이메일 인증이 되지 않았습니다", "checkAuth");
        }
        return sEmail;
    }  
    public static Map<Object,Object> getLoginInfor() {
        try {
            principalDetails principalDetails=(principalDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return principalDetails.getPrinci();
        } catch (Exception e) {
           throw makeRuntimeEX("로그인 정보를 불러오는데 실패했습니다", "getLoginInfor");
        }
    }
    public static int getLoginId(){
        try {
            return Integer.parseInt(getLoginInfor().get("id").toString());
        } catch (Exception e) {
           throw makeRuntimeEX("로그인 고유값 정보를 불러오는데 실패했습니다", "getLoginInfor");
        }
    }
    public static String getLoginEmail() {
        try {
            return getLoginInfor().get("email").toString();
        } catch (Exception e) {
           throw makeRuntimeEX("로그인 이메일 정보를 불러오는데 실패했습니다", "getLoginInfor");
        }
    }
    public static int getTotalPage(int totalCount,int pageSize) {
        int totalPage=totalCount/pageSize;
        int remainder=totalCount%pageSize;
        if(totalPage==0){
            return 1;
        }else if(remainder>0){
            totalPage+=1;
        }
        return totalPage;
    }
    public static int getStart(int page,int pageSize) {
        return (page-1)*pageSize+1;//+1 이있는 이유는 1페이질때 대응하기 위해이다  
    }
    public static List<String> getOnlyImgNames(String text) {
        List<String>imgPaths=getImgSrc(text);
        if(!imgPaths.isEmpty()){
            for(int i=0;i<imgPaths.size();i++){
                imgPaths.set(i, getImgNameInPath(imgPaths.get(i), 4));
            }
            return imgPaths;
        }else{
            writeLog("기존에 사용중이던 이미지가 존재하지 않습니다",utillService.class);
        }
        return imgPaths;
    }
    public static  List<String> getDateInStrgin(String keyword) {
        List<String>dates=Arrays.asList(keyword.split(","));
        if(dates.isEmpty()){
            dates.add(null);
            dates.add(null);
        }
        return dates;
    }
    public static Map<String,Object> checkRequestDate(String startDay,String endDay) {
        Map<String,Object>result=new HashMap<>();
        Boolean startResult=utillService.checkBlank(startDay);
        Boolean endResult=utillService.checkBlank(endDay);
        if(!startResult&&!endResult){
            Timestamp daystart=Timestamp.valueOf(startDay+" 00:00:00");
            Timestamp dayEnd=Timestamp.valueOf(endDay+" 23:59:59");
            //시작,종료일 검사
            if(daystart.toLocalDateTime().isAfter(dayEnd.toLocalDateTime())){
                throw utillService.makeRuntimeEX("기간을 제대로 설정해주세요", "getDelivers");
            }
            result.put("flag", true);
            result.put("start", daystart);
            result.put("end", dayEnd);
            return result;
        }else if(!startResult&&endResult){
            throw utillService.makeRuntimeEX("기간을 제대로 설정해주세요", "getDelivers");
        } 
        result.put("flag", false);
        return result;
    }
    public static <T> void writeLog(String message,Class<T> clazz) {
         Logger logger=LoggerFactory.getLogger(clazz);
         logger.info(message);
    }
    public static <T> String arrToLogString(T[] arr) {
        return Arrays.toString(arr);
    }
    public static <T> void checkDaoResult(List<T>arr,String errorMessage,String methodName) {
        if(utillService.checkEmthy(arr)){
            throw utillService.makeRuntimeEX(errorMessage, methodName);
        }
    }
    public static String getImgNameInPath(String imgPath,int num) {
        writeLog("분리할 이미지path: "+imgPath, utillService.class);
        return imgPath.split("/")[num];
    }
}
