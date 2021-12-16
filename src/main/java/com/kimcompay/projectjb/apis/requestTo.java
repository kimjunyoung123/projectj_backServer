package com.kimcompay.projectjb.apis;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class requestTo {
    private final  Logger logger=LoggerFactory.getLogger(requestTo.class);
    
    public <T> JSONObject requestPost(T body,String url,HttpHeaders headers) {
        logger.info("requestPost");
        try {
            //body+header합치기
            HttpEntity<T>entity=new HttpEntity<>(body,headers);
            logger.info("요청 정보: "+entity);
            logger.info("요청 url: "+url);
            RestTemplate restTemplate=new RestTemplate();
            return restTemplate.postForObject(url, entity, JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw utillService.makeRuntimeEX("통신에 실패하였습니다", "requestPost");
        }finally{
            headers.clear();
        }
    }
<<<<<<< HEAD
    
=======
    public <T> JSONObject requestGet(T body,String url,HttpHeaders headers) {
        logger.info("requestGet");
        try {
            RestTemplate restTemplate=new RestTemplate();
            HttpEntity<T>entity=new HttpEntity<>(body,headers);
            ResponseEntity<JSONObject>responseEntity=restTemplate.exchange(url,HttpMethod.GET,entity,JSONObject.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("통신에 실패했습니다","requestget");
        }
  
    }
>>>>>>> 5b4aea646582ebfbf91c8daa166f99d4f1acb45b
}
