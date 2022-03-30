package com.kimcompay.projectjb.payments.service;

import com.kimcompay.projectjb.payments.model.order.orderDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class orderService {
    @Autowired
    private orderDao orderDao;

    public void getOrder() {
        
    }
}
