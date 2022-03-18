package com.kimcompay.projectjb.apis.settle;

import com.kimcompay.projectjb.payments.model.pay.settleDto;

import org.springframework.stereotype.Service;

@Service
public class vbankService {
    
    public boolean cancleNotPayment(settleDto settleDto) {
        return true;
    }
}
