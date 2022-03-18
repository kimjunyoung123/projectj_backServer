package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class vbankService {
    
    @Autowired
    private requestTo requestTo;
    @Value("${settle.vbank.cancle.account.url}")
    private String cancleAccountUrl;

    public boolean cancleNotPayment(settleDto settleDto) {
        JSONObject reponse=requestTo.requestPost(makeCancleAccountBody(settleDto), cancleAccountUrl, utillService.getSettleHeader());
        utillService.writeLog("세틀뱅크 가상계좌 채번취소 통신결과: "+reponse.toString(), vbankService.class);

        return true;
    }
    private JSONObject makeCancleAccountBody(settleDto settleDto) {
        try {
            Map<String,String>map=utillService.getSettleTimeAndDate(LocalDateTime.now());
            String date=map.get("date");
            String time=map.get("time");
            String pktHash=requestcancleString(settleDto.getMchtTrdNo(),settleDto.getTrdAmt(), settleDto.getMchtId(),date,time,"0");
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", settleDto.getMchtId());
            params.put("ver", "0A19");
            params.put("method", "VA");
            params.put("bizType", "A2");
            params.put("encCd", "23");
            params.put("mchtTrdNo", settleDto.getMchtTrdNo());
            params.put("trdDt", date);
            params.put("trdTm", time);
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("orgTrdNo", settleDto.getTrdNo());
            data.put("vAcntNo", aes256.encrypt(settleDto.getVtlAcntNo()));
            body.put("params", params);
            body.put("data", data);
        return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm,String zero) {
        System.out.println("requestcancleString zero");
        return String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,zero,senums.settleKey.get()); 
    }
}
