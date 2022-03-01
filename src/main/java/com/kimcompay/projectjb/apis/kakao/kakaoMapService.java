package com.kimcompay.projectjb.apis.kakao;

import java.util.LinkedHashMap;
import java.util.List;

import com.kimcompay.projectjb.utillService;
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
    public List<LinkedHashMap<String,Object>> checkAddress(String address) {
        logger.info("checkAddress");
        JSONObject krespon=getAddress(address);
        logger.info("카카오 주소찾기 결과: "+krespon);
        LinkedHashMap<String,Object>meta=(LinkedHashMap<String, Object>)krespon.get("meta");
        if(Integer.parseInt(meta.get("total_count").toString())==0){
            logger.info("주소 검색결과 미존재");
            throw utillService.makeRuntimeEX("주소검색결과가 없습니다", "checkValues");
        }
        logger.info("주소 유효성 검사통과");
        return (List<LinkedHashMap<String,Object>>)krespon.get("documents");
    }
}
