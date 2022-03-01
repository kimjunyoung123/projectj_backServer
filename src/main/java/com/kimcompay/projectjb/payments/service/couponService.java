package com.kimcompay.projectjb.payments.service;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.coupon.couponDao;
import com.kimcompay.projectjb.payments.model.coupon.couponVo;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class couponService {
    
    @Autowired
    private couponDao couponDao;

    public JSONObject checkExist(String couponName) {
        confrimCoupon(getVo(couponName));
        return utillService.getJson(true, "사용가능 쿠폰");
    }
    public void confrimCoupon(couponVo couponVo) {
        
    }
    private couponVo getVo(String couponName) {
        return couponDao.findByName(couponName).orElseThrow(()->utillService.makeRuntimeEX("존재하지 않는 쿠폰", "checkExist"));
    }
}
