package com.kimcompay.projectjb.apis.sns;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@Service
public class smsService {
    private static Logger logger=LoggerFactory.getLogger(smsService.class);

    @Value("${coolsms.accesskey}")
    private  String apikey;
    @Value("${coolsms.pwdkey}")
    private  String apiSecret;
    @Value("${company.phone.num}")
    private  String company_phone;

    public  boolean sendMessege(String phoneNum,String messege) {
        logger.info(phoneNum+" 문자전송번호");
        Message coolsms = new Message(apikey, apiSecret);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNum);
        params.put("from",company_phone);
        params.put("type", "SMS");
        params.put("text", messege);
        try {
            coolsms.send(params);
            System.out.println("문자 전송 완료");
            return true;
        } catch (CoolsmsException e) {
            e.printStackTrace();
            System.out.println("sendMessege 전송 실패");
        }
       return false;
    }
}
