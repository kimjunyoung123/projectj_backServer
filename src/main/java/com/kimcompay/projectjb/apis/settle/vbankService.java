package com.kimcompay.projectjb.apis.settle;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.model.vbank.vbankDao;
import com.kimcompay.projectjb.payments.model.vbank.vbankVo;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class vbankService {
    
    @Autowired
    private requestTo requestTo;
    @Value("${settle.vbank.cancle.account.url}")
    private String cancleAccountUrl;
    @Autowired
    private vbankDao vbankDao;

   
    public JSONObject cancleDivision(Map<String,Object>orderAndPayments) {
        int state=Integer.parseInt(orderAndPayments.get("vbank_status").toString());
        if(state==0){
            return cancleAndGetReAccount(orderAndPayments);
        }else{
            
        }
        return null;
    }
    public JSONObject cancleAndGetReAccount(Map<String,Object>orderAndPayments){
        int totalPrice=Integer.parseInt(orderAndPayments.get("payment_total_price").toString());
        int minusPrice=Integer.parseInt(orderAndPayments.get("order_price").toString());
        int cancleTime=Integer.parseInt(orderAndPayments.get("cncl_ord").toString())+1;
        totalPrice=totalPrice-minusPrice;
        settleDto settleDto=new settleDto();
        settleDto.setMchtId(orderAndPayments.get("vbank_mcht_id").toString());
        settleDto.setMchtTrdNo(orderAndPayments.get("order_mcht_trd_no").toString());
        settleDto.setTrdAmt(Integer.toString(minusPrice));
        settleDto.setTrdNo(orderAndPayments.get("vbank_trd_no").toString());
        settleDto.setCnclOrd(cancleTime);
        settleDto.setFnCd(orderAndPayments.get("vbank_fn_cd").toString());
        settleDto.setVtlAcntNo(orderAndPayments.get("vtl_acnt_no").toString());
        //재채번
        if(totalPrice>0){   
            cancle(settleDto);
        }else{
            //채번취소
        }   
        return null;
    }
    public void cancle(settleDto settleDto) {
        JSONObject reponse=requestTo.requestPost(getCancleBody(settleDto), "https://tbgw.settlebank.co.kr/spay/APIRefund.do", utillService.getSettleHeader());
        utillService.writeLog("세틀뱅크 가상계좌 취소 통신결과: "+reponse.toString(), vbankService.class);
    }
    private JSONObject getReAccountBody(settleDto settleDto) {
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
        params.put("bizType", "A0");
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
    }
    public void insert(settleDto settleDto) {
        Timestamp expireDate=StringToTimestamp(settleDto.getExpireDt());
        vbankDao.save(dtoToVo(settleDto, expireDate));
    }
    private vbankVo dtoToVo(settleDto settleDto,Timestamp expireDate) {
    System.out.println(settleDto.getDpstrNm());
        return vbankVo.builder().cnclOrd(settleDto.getCnclOrd()).expireDt(expireDate).fnCd(settleDto.getFnCd()).fnNm(settleDto.getFnNm()).mchtId(settleDto.getMchtId()).mchtTrdNo(settleDto.getMchtTrdNo())
                        .dpstrNm(settleDto.getDpstrNm()).status(0).trdNo(settleDto.getTrdNo()).vtlAcntNo(settleDto.getVtlAcntNo()).vtrdDtm(Timestamp.valueOf(LocalDateTime.now())).build();
    }
    private Timestamp StringToTimestamp(String expireDate) { 
        System.out.println("time: "+expireDate);
        System.out.println("time2: "+expireDate.substring(0, 4)+"-"+expireDate.substring(5, 6)+"-"+expireDate.substring(7, 8)+" "+expireDate.substring(9,10)+":"+expireDate.substring(11, 12)+":"+expireDate.substring(13, 14));
        return Timestamp.valueOf(expireDate.substring(0, 4)+"-"+expireDate.substring(4, 6)+"-"+expireDate.substring(6, 7)+" "+expireDate.substring(8, 9)+":"+expireDate.substring(10, 11)+":"+expireDate.substring(12, 13));
    }
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
            throw new RuntimeException("가상계좌 채번취소 바디 생성에 실패하였습니다");
        }
    }
    private JSONObject getCancleBody(settleDto settleDto) {
        try {
            Map<String,String>map=utillService.getSettleTimeAndDate(LocalDateTime.now());
            String date=map.get("date");
            String time=map.get("time");
            String pktHash=requestcancleString(settleDto.getMchtTrdNo(),settleDto.getTrdAmt(), settleDto.getMchtId(),date,time);
            JSONObject body=new JSONObject();
            JSONObject params=new JSONObject();
            JSONObject data=new JSONObject();
            params.put("mchtId", "nx_mid_il");
            params.put("ver", "0A19");
            params.put("method", "VA");
            params.put("bizType", "C0");
            params.put("encCd", "23");
            params.put("mchtTrdNo", settleDto.getMchtTrdNo());
            params.put("trdDt", date);
            params.put("trdTm", time);
            data.put("crcCd", "KRW");
            data.put("cnclOrd", settleDto.getCnclOrd());
            data.put("pktHash", sha256.encrypt(pktHash));
            data.put("orgTrdNo", settleDto.getTrdNo());
            data.put("cnclAmt", aes256.encrypt(settleDto.getTrdAmt()));
            data.put("refundBankCd",settleDto.getFnCd());
            data.put("refundAcntNo", aes256.encrypt(settleDto.getVtlAcntNo()));
            data.put("refundDpstrNm", "김준영");
            body.put("params", params);
            body.put("data", data);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("가상계좌 취소 바디 생성에 실패하였습니다");
        }
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm,String zero) {
        System.out.println("requestcancleString zero");
        return String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,zero,senums.settleKey.get()); 
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        System.out.println("requestcancleString");
        return  String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,senums.settleKey.get()); 
    }

    
}
