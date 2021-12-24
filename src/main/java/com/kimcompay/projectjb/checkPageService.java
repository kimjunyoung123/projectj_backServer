package com.kimcompay.projectjb;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class checkPageService {
    private Logger logger=LoggerFactory.getLogger(checkPageService.class);
    @Autowired
    private RedisTemplate<String,String>redisTemplate;

    public JSONObject checkPage(HttpServletRequest request,String scope) {
        logger.info("checkPage");
        String val=request.getParameter("val");
        logger.info("값: "+val);
        if(scope.equals("redis")){
            checkRedis(val);
        }else{
            throw utillService.makeRuntimeEX("유요한 검증 값이 아닙니다", "checkPage");
        }
        return utillService.getJson(true, "유효한 요청");
    }
    private void checkRedis(String val) {
        logger.info("checkRedis");
        //요청이 있었는지검사
        Map<Object,Object>redis=redisTemplate.opsForHash().entries(val);
        System.out.println(redis);
        if(utillService.checkEmthy(redis)){
            throw utillService.makeRuntimeEX("잘못된요청입니다", "checkRedis");
        }
    }
}
