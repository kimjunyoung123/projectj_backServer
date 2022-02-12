package com.kimcompay.projectjb.users.company;

import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.users.company.model.tryProductInsertDto;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class productService {
    private Logger logger=LoggerFactory.getLogger(productService.class);

    public JSONObject insert(tryProductInsertDto tryProductInsertDto) {
        logger.info("insert");
        checkEvent(tryProductInsertDto.getEventFlag(), tryProductInsertDto.getEventInfors());
        return null;
    }
    private void checkEvent(boolean eventFlag,List<Map<String,Object>>eventInfors) {
        logger.info("checkEvent");
        if(eventFlag){
            logger.info("이벤트가 있는 제품");
            for(Map<String,Object>eventInfor:eventInfors){
                if(Integer.parseInt(eventInfor.get("price").toString().replace(",", ""))<=0){
                    throw utillService.makeRuntimeEX("이벤트가격이 0원 보다 작거나 같습니다 \n 날짜: "+eventInfor.get("date"), "checkEvent");
                }
            }
        }
        logger.info("이벤트 유효성 검사 통과");
    }
}
