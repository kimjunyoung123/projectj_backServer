package com.kimcompay.projectjb.payments.service;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.pay.tryOrderDto;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class paymentService {

    public JSONObject tryOrder(tryOrderDto tryOrderDto) {
        JSONObject respons=new JSONObject();
        respons.put("price", aes256.encrypt( "1000"));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText("nxca_jt_il", "card", "1234567", "20220301", "153500", "1000")));
        respons.put("flag", true);
        return respons;
    }
}
