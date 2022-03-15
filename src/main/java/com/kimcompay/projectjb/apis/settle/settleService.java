package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.helpPaymentService;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;
import com.kimcompay.projectjb.users.company.model.products.productVo;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class settleService {
    
    @Autowired
    private cardService cardService;
    @Autowired
    private helpPaymentService helpPaymentService;
    @Value("${front.domain}")
    private String frontDomain;
    @Value("${front.result.page}")
    private String resultLink;
    
    @Transactional(rollbackFor = Exception.class)
    public void confrimPayment(String kind,String status,settleDto settleDto) {
        //결제 성공/실패 판단
        String mchtTrdNo=settleDto.getMchtTrdNo();
        boolean result=false;
        String message=null;
        if(senums.paySuc.get().equals(settleDto.getOutStatCd())){
            int paymentPrice=Integer.parseInt(utillService.aesToNomal(settleDto.getTrdAmt()));
            if(helpPaymentService.confrimPaymentAndInsert(mchtTrdNo, paymentPrice)){
                String method=settleDto.getMethod();
                if(method.equals("card")){
                    cardService.insert(settleDto);
                }else if(method.equals("vbank")){

                }
                message="결제가 완료 되었습니다";
                result=true;
            }else{
                //환불로직
            }
        }else{
            String outRsltCd=settleDto.getOutRsltCd();
            utillService.writeLog("세틀뱅크 결제 실패 이유: "+outRsltCd+" 결제번호: "+mchtTrdNo, settleService.class);
            message="결제에 실패하였습니다";//나중에 메세지 붙혀주면됨
        }
        String url=frontDomain+resultLink+"?kind=settle&action=payment&result="+result+"&message="+message;
        utillService.doRedirect(utillService.getHttpSerResponse(), url);
    }
    private void tryInsert(String kind,settleDto settleDto) {
        
    }
    public JSONObject makeRequestPayInfor(String productNames,paymentVo paymentVo,List<orderVo>orders,String mchtId,String expireIfVank) { 
        //응답 생성
        JSONObject respons=new JSONObject();
        String mchtTrdNo=paymentVo.getMchtTrdNo();
        String method=paymentVo.getMethod();
        Map<String,String>dateAndTime=utillService.getSettleTimeAndDate(LocalDateTime.now());
        String date=dateAndTime.get("date");
        String time=dateAndTime.get("time");
        if(expireIfVank!=null){
            respons.put("expireDt",expireIfVank);
        }
        respons.put("method", method);
        respons.put("mchtId", mchtId);
        respons.put("mchtCustId", aes256.encrypt(Integer.toString(utillService.getLoginId())));
        respons.put("date", date);
        respons.put("time", time);
        respons.put("mchtTrdNo", mchtTrdNo);
        respons.put("productNames",new StringBuffer(productNames).deleteCharAt(productNames.length()-1));
        respons.put("price", aes256.encrypt( Integer.toString(paymentVo.getTotalPrice())));
        respons.put("pktHash", sha256.encrypt(utillService.getSettleText(mchtId,method,mchtTrdNo, date, time, Integer.toString(paymentVo.getTotalPrice()))));
        respons.put("flag", true);
        return respons;
    }
}
