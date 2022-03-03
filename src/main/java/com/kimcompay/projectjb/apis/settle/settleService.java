package com.kimcompay.projectjb.apis.settle;

import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class settleService {
    
    public JSONObject makeRequestPayInfor(Map<String,Object> orderAndproduct) {
        JSONObject respons=new JSONObject();
        respons.put("price", aes256.encrypt( "1000"));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText("nxca_jt_il", "card", "1234567", "20220301", "153500", "1000")));
        respons.put("flag", true);
        return respons;
    }
}
