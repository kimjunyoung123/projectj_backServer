package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;
import com.kimcompay.projectjb.users.company.model.products.productVo;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class settleService {
    
    @Transactional(rollbackFor = Exception.class)
    public JSONObject makeRequestPayInfor(Map<String,Object> orderAndproduct) {
        paymentVo paymentVo=(paymentVo)orderAndproduct.get("payment");
        List<orderVo>orders=(List<orderVo>)orderAndproduct.get("orders");

        //응답 생성
        JSONObject respons=new JSONObject();
        String mchtTrdNo=paymentVo.getMchtTrdNo();
        String method=paymentVo.getMethod();
        Map<String,String>dateAndTime=utillService.getSettleTimeAndDate(LocalDateTime.now());
        String date=dateAndTime.get("date");
        String time=dateAndTime.get("time");
        respons.put("date", date);
        respons.put("time", time);
        respons.put("mchtTrdNo", mchtTrdNo);
        respons.put("productNames",new StringBuffer(orderAndproduct.get("productNames").toString()).deleteCharAt(orderAndproduct.get("productNames").toString().length()-1));
        respons.put("price", aes256.encrypt( Integer.toString(paymentVo.getTotalPrice())));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText("nxca_jt_il",method,mchtTrdNo, date, time, Integer.toString(paymentVo.getTotalPrice()))));
        respons.put("flag", true);
        return respons;
    }
}
