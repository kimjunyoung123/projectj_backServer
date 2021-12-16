package com.kimcompay.projectjb.apis.kakaos;

import com.kimcompay.projectjb.apis.requestTo;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class kakaoMapService {
    
    private Logger logger=LoggerFactory.getLogger(kakaoMapService.class);

    @Autowired
    private requestTo requestTo;
    public JSONObject getAddress(String address) {
        logger.info("getAddress");
        return null;
    }
}
