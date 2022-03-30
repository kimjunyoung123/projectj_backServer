package com.kimcompay.projectjb.payments.service;

import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class orderService {
    @Autowired
    private orderDao orderDao;

    public JSONObject getOrders(String mchtTrdNo,int storeId) {
        List<Map<String,Object>>paymentAndOrders=orderDao.findByJoinProductMchtTrdNo(mchtTrdNo,storeId);
        utillService.checkDaoResult(paymentAndOrders, "내역이 존재 하지 않습니다", "getJoinOrders");
        return utillService.getJson(true, paymentAndOrders);
    }
}
