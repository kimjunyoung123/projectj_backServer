package com.kimcompay.projectjb;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.apis.kakao.kakaoService;
import com.kimcompay.projectjb.apis.naver.naverService;
import com.kimcompay.projectjb.enums.senums;

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
    @Autowired
    private kakaoService kakaoService;
    @Autowired
    private naverService naverService;

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
    public JSONObject selectPage(String kind,String action) {
        logger.info("selectPage");
        logger.info("요청 소셜: "+kind);
        logger.info("요청 액션: "+action);
        if(kind.equals(senums.kakao.get())){
            logger.info("카카오요청");
            return kakaoService.callPage(action);
        }else if(kind.equals(senums.naver.get())){
            logger.info("네이버 요청");
            return naverService.callPage(action);
        }
        throw utillService.makeRuntimeEX("지원하지 않는 소셜서비스입니다", "selectPage");
    }
    public void selectCallback(String kind,String action,HttpServletRequest request) {
        logger.info("selectCallback");
        logger.info("콜백 소셜: "+kind);
        logger.info("콜백 액션: "+action);
        if(kind.equals(senums.kakao.get())){
            logger.info("카카오콜백 요청");
        }else if(kind.equals(senums.naver.get())){
            logger.info("네이버 콜백 요청");
            
        }else{
            throw utillService.makeRuntimeEX("지원하지 않는 소셜서비스입니다", "selectPage");
        }
        logger.info("forword");
    }
}
