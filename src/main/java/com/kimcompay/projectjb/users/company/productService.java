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
        checkValues(tryProductInsertDto.getPrice(),tryProductInsertDto.getEventFlag(), tryProductInsertDto.getEventInfors());
        if(tryProductInsertDto.getEventFlag()){
            logger.info("이벤트 테이블 insert시도");

        }
        return null;
    }
    private void checkValues(String price,boolean eventFlag,List<Map<String,Object>>eventInfors) {
        logger.info("checkValue");
        System.out.println(price.length()+"길이~");
        //가격유효성검사
        if(checkPrice(price)){
            throw utillService.makeRuntimeEX("가격이 0원입니다", "checkValue");
        }
        checkEvent(eventFlag, eventInfors);

    }
    private boolean checkPrice(String price) {
        logger.info("checkPrice");
        if(Integer.parseInt(price.replace(",", ""))<=0){
            return true;
        }else if(price.length()>3&&!price.contains(",")){
            throw utillService.makeRuntimeEX("1,000원이상은 쉼표로 구분해주세요", "checkValue");
        }else if(price.contains(",")){
            
            char[] c=price.toCharArray();
            for(int i=0;i<c.length;i++){
                int cn=(int)c[i];
                if(cn<48||cn>57){
                    throw utillService.makeRuntimeEX("가격형식이 올바르지 않습니다", "checkValue");
                }
            }
        }
        return false;
    }
    private void checkEvent(boolean eventFlag,List<Map<String,Object>>eventInfors) {
        logger.info("checkEvent");
        if(eventFlag){
            logger.info("이벤트가 있는 제품");
            for(Map<String,Object>eventInfor:eventInfors){
                if(checkPrice(eventInfor.get("price").toString())){
                    throw utillService.makeRuntimeEX("이벤트가격이 0원 보다 작거나 같습니다 \n 날짜: "+eventInfor.get("date"), "checkEvent");

                }
            }
        }
        logger.info("이벤트 유효성 검사 통과");
    }
}
