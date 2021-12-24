package com.kimcompay.projectjb.apis.naver;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.nenum;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class naverService {
    private Logger logger=LoggerFactory.getLogger(naverService.class);

    public JSONObject callPage(String action) {
        logger.info("callPage");
        String url=null;
        try {
            if(action.equals(nenum.loginPage.get())){
                logger.info("로그인페이지요청");
                url=null;
            }
            return utillService.getJson(true, url);
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("지원하지 않는 네이버기능 ", "callPage");
        }
    }
}
