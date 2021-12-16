package com.kimcompay.projectjb.apis.jungbu;

import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.apis.requestTo;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class jungbuService {
    private Logger logger=LoggerFactory.getLogger(jungbuService.class);

    @Value("${tax.decoding.apikey}")
    private String apikey;
    @Autowired
    private requestTo requestTo;

    public JSONObject getCompanyNum(int compay_num) {
        logger.info("getCompanyNum");
        //body생성
        JSONObject body=new JSONObject();
        //사업자등록증 담기
        List<String>integers=new ArrayList<>();
        integers.add(Integer.toString(compay_num));
        body.put("b_no", integers);
        //요청 url
        String url="https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey="+apikey;
        //헤더담기
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return requestTo.requestPost(body, url, headers);
    }
}
