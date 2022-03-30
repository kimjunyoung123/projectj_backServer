package com.kimcompay.projectjb.payments.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.sqsService;
import com.kimcompay.projectjb.apis.sns.snsService;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.users.user.userService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class orderService {
    @Autowired
    private orderDao orderDao;
    @Autowired
    private sqsService sqsService;
    @Autowired
    private userService userService;

    public JSONObject getOrders(String mchtTrdNo,int storeId) {
        List<Map<String,Object>>paymentAndOrders=orderDao.findByJoinProductMchtTrdNo(mchtTrdNo,storeId);
        utillService.checkDaoResult(paymentAndOrders, "내역이 존재 하지 않습니다", "getJoinOrders");
        return utillService.getJson(true, paymentAndOrders);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject changeOrderState(String mchtTrdNo,int storeId,int state) {
        int userId=changeState(mchtTrdNo, storeId, state);
        if(userId!=0){
            sendStart(userId,state);
            return utillService.getJson(true, "장보기 상태가 변경되었습니다");
        }else{
            return utillService.getJson(true, "이미 진행중인 행위입니다 중복 클릭시 메세지");
        }
    }
    private int changeState(String mchtTrdNo,int storeId,int state) {
        List<orderVo>orderVos=orderDao.findByMchtTrdNoAndStoreId(mchtTrdNo, storeId);
        utillService.checkDaoResult(orderVos, "내역이 존재하지 않습니다", "changeOrderState");
        boolean flag=false;
        for(orderVo vo:orderVos){
            if(vo.getCancleFlag()!=state){
                flag=true;
            }
            vo.setCancleFlag(state);
        }
        if(flag){
            return orderVos.get(0).getUserId();
        }else{
            return 0;
        }
    }
    @Async
    public void sendStart(int userId,int  state) {
        List<String>scopes=new ArrayList<>();
        scopes.add("email");
        scopes.add("phone");
        Map<String,Object>emailAndPhone=userService.getUserInfor(userId, scopes);
        String text="";
        if(state==2){
            text="장보기가 시작되었습니다";
        }else if(state==3){
            text="장보기가 취소 되었습니다";
        }else if(state==4){
            text="장보기가 완료되었습니다";
        }
        sqsService.sendEmailAsync("안녕하세요 장보고 입니다 \n"+text, emailAndPhone.get("email").toString());
        sqsService.sendPhoneAsync("안녕하세요 장보고 입니다 \n"+text, emailAndPhone.get("phone").toString());
    }
}
