package com.kimcompay.projectjb.payments;

import javax.validation.Valid;

import com.kimcompay.projectjb.payments.model.pay.tryOrderDto;
import com.kimcompay.projectjb.payments.service.couponService;
import com.kimcompay.projectjb.payments.service.paymentService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/payment")
public class paymentAuthRestController {

    @Autowired
    private couponService couponService;
    @Autowired
    private paymentService paymentService;
    
    //쿠폰 검사
    @RequestMapping(value = "/coupon/{couponName}",method = RequestMethod.GET)
    public JSONObject confrimCoupon(@PathVariable String couponName) {
        return couponService.checkExist(couponName);
    }
    //구매시도 ,거리 계산
    @RequestMapping(value = "",method = RequestMethod.POST)
    public JSONObject tryPay(@Valid @RequestBody tryOrderDto tryOrderDto) {
        return paymentService.tryOrder(tryOrderDto);
    }
    
}
