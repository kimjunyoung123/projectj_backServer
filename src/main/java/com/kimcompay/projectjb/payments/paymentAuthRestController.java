package com.kimcompay.projectjb.payments;

import com.kimcompay.projectjb.payments.service.couponService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/payment")
public class paymentAuthRestController {

    @Autowired
    private couponService couponService;
    
    @RequestMapping(value = "/coupon/{couponName}",method = RequestMethod.GET)
    public JSONObject confrimCoupon(@PathVariable String couponName) {
        return couponService.checkExist(couponName);
    }
}
