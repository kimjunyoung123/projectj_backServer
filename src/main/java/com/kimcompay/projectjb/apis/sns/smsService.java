package com.kimcompay.projectjb.apis.sns;

import java.util.HashMap;

import com.kimcompay.projectjb.utillService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;

@Service
public class smsService {

    @Value("${coolsms.accesskey}")
    private  String apikey;
    @Value("${coolsms.pwdkey}")
    private  String apiSecret;
    @Value("${company.phone.num}")
    private  String company_phone;

    public  boolean sendMessege(String phoneNum,String messege) {
        Message coolsms = new Message(apikey, apiSecret);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phoneNum);
        params.put("from",company_phone);
        params.put("type", "SMS");
        params.put("text", messege);
        try {
            coolsms.send(params);
            return true;
        } catch (CoolsmsException e) {
            e.printStackTrace();
           utillService.writeLog("sendMessege 전송 실패",smsService.class);
        }
       return false;
    }
}
