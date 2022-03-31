package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.exceptions.paymentFailException;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.helpPaymentService;
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
    private vbankService vbankService;
    @Autowired
    private cardService cardService;
    @Autowired
    private helpPaymentService helpPaymentService;
    @Value("${front.domain}")
    private String frontDomain;
    @Value("${front.result.page}")
    private String resultLink;
    
    public String canclePay(settleDto settleDto) {
        String method=settleDto.getMethod();
        String message="결제실패$환불실패";
        if(method.equals("card")){
            if(cardService.cancle(settleDto)){
                method="결제실패$환불성공";
            }   
        }else if(method.equals("vbank")){
            if(vbankService.cancleNotPayment(settleDto)){
                method="결제실패$채번이취소되었습니다";
            }else{
                message="다시시도 해주세요";
            } 
        }
        return message;
    }
    public void cancleByStore(Map<String,Object>orderAndPayments,String method) {
        if(method.equals(senums.cardText.get())){
            
        }else if(method.equals(senums.vbankText.get())){
            vbankService.cancleDivision(orderAndPayments);
        }else{
            throw utillService.makeRuntimeEX("세틀뱅크에서 지원하지 않는 결제수단입니다", "cancleByStore");
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void confrimPayment(String kind,String status,settleDto settleDto) throws paymentFailException{
        //결제 성공/실패 판단
        String mchtTrdNo=settleDto.getMchtTrdNo();
        String message=null;
        String state=settleDto.getOutStatCd();
        if(senums.paySuc.get().equals(state)||"0051".equals(state)){
            int paymentPrice=Integer.parseInt(utillService.aesToNomal(settleDto.getTrdAmt()));
            //예외발생시 대처 위해
            settleDto.setTrdAmt(Integer.toString(paymentPrice));
            //가상계좌라면 계좌 복호화
            if(settleDto.getMethod().equals("vbank")){
                settleDto.setVtlAcntNo(utillService.aesToNomal(settleDto.getVtlAcntNo()));
            }
            try {
                helpPaymentService.confrimPaymentAndInsert(mchtTrdNo, paymentPrice);
                tryInsert(kind, settleDto);
            } catch (Exception e) {
                //환불시 취소 회차 올려줘야함
                settleDto.setCnclOrd(1);
                throw new paymentFailException(settleDto, kind, e.getMessage());
            }
        }else{
            //애초에 결제가 안됐으므로 환불 불필요
            String outRsltCd=settleDto.getOutRsltCd();
            message="세틀뱅크 결제 실패 이유: "+outRsltCd+" 결제번호: "+mchtTrdNo;
            utillService.writeLog(message, settleService.class);
            message="결제에 실패하였습니다";//나중에 메세지 붙혀주면됨
        }
        //redirect함수 
    }
    private void tryInsert(String kind,settleDto settleDto) {
        if(kind.equals(senums.cardText.get())){
            cardService.insert(settleDto);
        }else if(kind.equals(senums.vbankText.get())){
            vbankService.insert(settleDto);
        }
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
