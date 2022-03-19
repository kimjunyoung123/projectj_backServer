package com.kimcompay.projectjb.payments.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.order.orderDao;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class orderService {
    private final int pageSize=2;
    @Autowired
    private orderDao orderDao;

    public JSONObject getOrders(int page,String start,String end) {
        List<Map<String,Object>>dtos=getDtosByUserId(page, start, end);
        if(dtos.isEmpty()){
            throw utillService.makeRuntimeEX("내역을 찾을 수 없습니다", "getPayments");
        }
        int totalPage=utillService.getTotalPage(Integer.parseInt(dtos.get(0).get("totalCount").toString()), pageSize);
        JSONObject response=new JSONObject();
        response.put("message", dtos);
        response.put("totalPage", totalPage);
        response.put("flag", true);
        return response;
    }
    private List<Map<String,Object>> getDtosByUserId(int page,String start,String end) {
        int userId=utillService.getLoginId();        
        boolean startFlag=utillService.checkBlank(start);
        boolean endFlag=utillService.checkBlank(end);
        if(startFlag&&endFlag){
            return orderDao.findByUserIdPagIng(userId,userId,utillService.getStart(page, pageSize)-1,pageSize);
        }else if((startFlag&&endFlag==false)||(startFlag==false&&endFlag)){
            throw utillService.makeRuntimeEX("일자를 제대로 선택해 주세요", "getDtosByUserId");
        }
        Timestamp startDate=Timestamp.valueOf(start);
        Timestamp endDate=Timestamp.valueOf(end);
        if(startDate.toLocalDateTime().isAfter(endDate.toLocalDateTime())){
            throw utillService.makeRuntimeEX("날짜 범위가 유효하지 않습니다", "getDtosByUserId");
        }
        return orderDao.findByUserIdPagIng(userId,startDate,endDate,userId,startDate,endDate,utillService.getStart(page, pageSize)-1,pageSize);
    }
}
