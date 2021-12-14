package com.kimcompay.projectjb.apis;

import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class requestTo {
    private final  Logger logger=LoggerFactory.getLogger(requestTo.class);
    
    public <T> JSONObject requestPost(T body,String url,HttpHeaders headers) {
        logger.info("requestToApi");
        try {
            RestTemplate restTemplate=new RestTemplate();
            HttpEntity<T>entity=new HttpEntity<>(body,headers);
            System.out.println(entity.toString());
            return restTemplate.postForObject(url, entity, JSONObject.class);
        } catch (Exception e) {
            e.getStackTrace();
            throw utillService.makeRuntimeEX("통신에 실패하였습니다", "requestToApi");
        }finally{
            headers.clear();
        }
    }
}
