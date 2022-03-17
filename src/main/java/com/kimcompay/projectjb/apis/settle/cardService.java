package com.kimcompay.projectjb.apis.settle;

import java.time.LocalDateTime;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.payments.model.card.cardDao;
import com.kimcompay.projectjb.payments.model.card.cardVo;
import com.kimcompay.projectjb.payments.model.pay.settleDto;
import com.kimcompay.projectjb.payments.service.aes256;
import com.kimcompay.projectjb.payments.service.sha256;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class cardService {
    
    @Autowired
    private cardDao cardDao;
    @Autowired
    private requestTo requestTo;

    public boolean cancle(settleDto settleDto) {
        requestTo.requestPost(body, url, utillService.getSettleHeader());
    }
    public void insert(settleDto settleDto) {
        cardDao.save(dtoToVo(settleDto));   
    }
    private cardVo dtoToVo(settleDto settleDto) {
        cardVo vo=cardVo.builder().authNo(settleDto.getAuthNo()).cancleFlag(0).fnCd(settleDto.getFnCd()).fnNm(settleDto.getFnNm()).intMon(settleDto.getIntMon())
                                    .mchtTrdNo(settleDto.getMchtTrdNo()).orgTrdNo(settleDto.getTrdNo()).paymentId(settleDto.getMchtTrdNo()).build();
                                    return vo;
    }
    public JSONObject makecancelBody(settleDto settleDto) {
        Map<String,String>map=utillService.getSettleTimeAndDate(LocalDateTime.now());
        String pktHash=requestcancleString(settleDto.getMchtTrdNo(),settleDto.getTrdAmt(), settleDto.getMchtId(),map.get("trdDt"),map.get("trdTm"));
        System.out.println(settleDto.getTrdAmt());
        JSONObject body=new JSONObject();
        JSONObject params=new JSONObject();
        JSONObject data=new JSONObject();
        params.put("mchtId", settleDto.getMchtId());
        params.put("ver", "0A17");
        params.put("method", "CA");
        params.put("bizType", "C0");
        params.put("encCd", "23");
        params.put("mchtTrdNo", settleDto.getMchtTrdNo());
        params.put("trdDt", map.get("trdDt"));
        params.put("trdTm",map.get("trdTm"));
        data.put("cnclOrd", settleDto.getCnclOrd());
        data.put("pktHash", sha256.encrypt(pktHash));
        data.put("orgTrdNo", settleDto.getTrdNo());
        data.put("crcCd", "KRW");
        data.put("cnclAmt", aes256.encrypt(settleDto.getTrdAmt()));
        body.put("params", params);
        body.put("data", data);
       
        return body;
    }
    private String requestcancleString(String mchtTrdNo,String price,String mchtId,String trdDt,String trdTm) {
        System.out.println("requestcancleString");
        return  String.format("%s%s%s%s%s%s",trdDt,trdTm,mchtId,mchtTrdNo,price,"ST1009281328226982205"); 
    }

}
