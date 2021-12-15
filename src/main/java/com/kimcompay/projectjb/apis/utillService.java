package com.kimcompay.projectjb.apis;

import java.util.Random;

import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class utillService {
    private final static Logger logger=LoggerFactory.getLogger(requestTo.class);

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

}
