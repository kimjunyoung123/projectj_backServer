package com.kimcompay.projectjb.apis.kakao;

import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


@Service
public class kakaoMapService {
    
    private Logger logger=LoggerFactory.getLogger(kakaoMapService.class);
    @Value("${kakao.app.key}")
    private String app_key;
    @Value("${kakao.rest.key}")
    private String rest_key;
    @Value("${kakao.js.key}")
    private String js_key;
    @Value("${kakao.admin.key}")
    private String admin_key;

    @Autowired
    private requestTo requestTo;

    public JSONObject getAddress(String address) {
        logger.info("getAddress");
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.add(senums.Authorization.get(),"KakaoAK "+rest_key);
        return  requestTo.requestGet(null,"https://dapi.kakao.com/v2/local/search/address.json?query="+address,httpHeaders);
    }
}
