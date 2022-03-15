package com.kimcompay.projectjb.payments.service;

import java.time.LocalDateTime;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.coupon.couponDao;
import com.kimcompay.projectjb.payments.model.coupon.couponVo;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class couponService {
    
    @Autowired
    private couponDao couponDao;

    //상위에 트랜잭셔널 있음
    public void changeState(int usedFlag,String couponName,String mchtTrdNo) {
        couponVo couponVo=CheckAndGet(couponName);
        couponVo.setUsed(usedFlag);
        couponVo.setMchtTrdNo(mchtTrdNo);
    }
    public couponVo CheckAndGet(String couponName) {
        couponVo couponVo=getVo(couponName);
        confrimCoupon(couponVo);
        return couponVo;
    }
    public JSONObject checkExist(String couponName) {
        String[] couponNames=couponName.split(",");
        for(String coupon:couponNames){
            confrimCoupon(getVo(coupon));
        }
        return utillService.getJson(true, "사용가능 쿠폰");
    }
    public void confrimCoupon(couponVo couponVo) {
        String message=null;
        if(LocalDateTime.now().isAfter(couponVo.getExpire().toLocalDateTime())){
            message="유효기간이 지난 쿠폰입니다 \n쿠폰이름: "+couponVo.getName();
        }else if(couponVo.getUsed()==1){
            message="이미 사용된 쿠폰입니다 \n쿠폰이름: "+couponVo.getName();
        }else{
            utillService.writeLog("쿠폰 검사통과", couponService.class);
            return;
        }
        throw utillService.makeRuntimeEX(message, "confrimCoupon");
    }
    private couponVo getVo(String couponName) {
        return couponDao.findByName(couponName).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 쿠폰", "checkExist"));
    }
}
