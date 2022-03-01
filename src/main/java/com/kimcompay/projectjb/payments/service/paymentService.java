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
        respons.put("price", aes256Service.encrypt( "500"));
        respons.put("pktHash", sha256Service.sha256(utillService.getSettleText("nxca_jt_il", "card", "12345", "20220301", "153500", "500")));
        respons.put("flag", true);
        return respons;
    }
}
