package com.kimcompay.projectjb.delivery.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailDao;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailVo;
import com.kimcompay.projectjb.delivery.model.deliveryRoomDao;
import com.kimcompay.projectjb.delivery.model.deliveryRoomVo;
import com.kimcompay.projectjb.delivery.model.tryInsertDto;
import com.kimcompay.projectjb.payments.service.orderService;
import com.kimcompay.projectjb.payments.service.paymentService;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class deliveryService {
    private final int pageSize=2;

    @Autowired
    private deliveryRoomDao deliveryRoomDao;
    @Autowired
    private deliverRoomDetailDao deliverRoomDetailDao;
    @Autowired
    private paymentService paymentService;
    
    public int countRoomByRoomId(int storeId,int roomId) {
        return deliveryRoomDao.countByStoreIdAndRoomId(storeId, roomId);
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject makeDeliverRoom(tryInsertDto tryInsertDto,int storeId) {
        List<List<Map<String,Object>>>ordersAndPayment=new ArrayList<>();
        List<String>mchtTrdNos=tryInsertDto.getMchtTrdNos();
        for(String mchtTrdNo:mchtTrdNos){
            if(deliverRoomDetailDao.countByMchtTrdNo(mchtTrdNo)>=1){
                throw utillService.makeRuntimeEX("이미 다른 배달방에 들어있습니다 \n결제번호: "+mchtTrdNo, "makeDeliverRoom");
            }
            ordersAndPayment.add(paymentService.getPaymentAndOrdersUseDeliver(mchtTrdNo, storeId));
        }
        if(ordersAndPayment.isEmpty()){
            throw utillService.makeRuntimeEX("내역이 존재하지 않습니다", "makeDeliverRoom");
        }
        System.out.println(ordersAndPayment.size());
        deliveryRoomVo deliveryRoomVo=new deliveryRoomVo();
        deliveryRoomVo.setCompanyId(utillService.getLoginId());
        deliveryRoomVo.setDeliverRoomFlag(0);
        deliveryRoomVo.setStoreId(storeId);
        deliveryRoomDao.save(deliveryRoomVo);
        for(List<Map<String,Object>> ordersAndPayments:ordersAndPayment){
            for(Map<String,Object>orderAndPayment:ordersAndPayments){
                if(orderAndPayment.get("cancle_all_flag").equals("1")){
                    throw utillService.makeRuntimeEX("전액 환불된 배달이 있습니다 \n결제번호:"+orderAndPayment.get("order_mcht_trd_no"), "makeDeliverRoom");
                }
                System.out.println(orderAndPayment.get("order_mcht_trd_no".toString()));
                deliverRoomDetailVo deliverRoomDetailVo=new deliverRoomDetailVo();
                deliverRoomDetailVo.setAddress(orderAndPayment.get("payment_postcode")+"/"+orderAndPayment.get("payment_address")+"/"+orderAndPayment.get("payment_detail_address"));
                deliverRoomDetailVo.setMchtTrdNo(orderAndPayment.get("order_mcht_trd_no").toString());
                deliverRoomDetailVo.setUserId(Integer.parseInt(orderAndPayment.get("user_id").toString()));
                deliverRoomDetailVo.setDoneFlag(0);
                deliverRoomDetailVo.setRoomId(deliveryRoomVo.getRoomId());
                deliverRoomDetailDao.save(deliverRoomDetailVo);
            }
        }
        return utillService.getJson(true, "배달방이 만들어 졌습니다");
    }
    public JSONObject getDeliverAddress(int roomId) {
        return utillService.getJson(true, deliverRoomDetailDao.findAddressByRoomId(roomId));
    }
    public JSONObject getDelivers(int page,String keyword,int storeId) {
        List<String>dates=utillService.getDateInStrgin(keyword);
        List<Map<String,Object>>deliveryInfors=selectByPeriodAndStoreId(page, dates.get(0),dates.get(1), storeId);
        //요청페이지,배달건수 검증
        if(utillService.checkEmthy(deliveryInfors)){ 
            throw utillService.makeRuntimeEX("배달건수가 존재하지 않습니다", "getDelivers");
        } 
        int totalPage=utillService.getTotalPage(Integer.parseInt(deliveryInfors.get(0).get("totalCount").toString()),pageSize);
        //본인 가게 배달방이 맞나 검사
        for(Map<String,Object> deliveryInfor:deliveryInfors ){
            utillService.checkOwner(Integer.parseInt(deliveryInfor.get("company_id").toString()),"회사 소유 매장 배달이 아닙니다");
        }
        JSONObject response =new JSONObject();
        response.put("flag", true);
        response.put("totalPage",  totalPage);
        response.put("message", deliveryInfors);
        return response;
    }
    public List<Map<String,Object>> selectByPeriodAndStoreId(int page,String startDay,String endDay,int storeId) {
        Map<String,Object>result=utillService.checkRequestDate(startDay, endDay);
        if((boolean)result.get("flag")){
            Timestamp start=Timestamp.valueOf(result.get("start").toString());
            Timestamp end=Timestamp.valueOf(result.get("end").toString());
            return deliveryRoomDao.findByDay(start, end, storeId,start, end, storeId,utillService.getStart(page, pageSize)-1,pageSize);

        }
        return deliveryRoomDao.findByAll(storeId,storeId,utillService.getStart(page, pageSize)-1,pageSize);
    }
    public List<Integer> selectRoomIdByUserIdAndFlag(int userId,int flag) {
        return deliverRoomDetailDao.findAllByRoomIdAndDoneFlag(userId, flag);
    }
   /* public List<Integer> selectRoomIdByCompanyIdAndStartDoneFlag(int companyId,int startFlag,int doneFlag) {
        logger.info("selectRoomIdByCompanyIdAndStartDoneFlag");
        return deliveryRoomDao.findAllByMasterIdAndFlag(companyId, doneFlag, startFlag);
    }*/
    /*@Transactional(rollbackFor = Exception.class)
    public void enterRoom(int roomId,String userSocketId,int userId) {
        logger.info("enterRoom");
        //배송요청조회 로직추가해야함 
        //배송방정보조회 null=첫배송조회 페이지 입장인경우 빈객체 생성
        logger.info(roomId+""+userId);
        deliverRoomDetailVo vo=deliverRoomDetailDao.findByFlagAndUserId(roomId,0, userId).orElseGet(()->new deliverRoomDetailVo());
        //logger.info(vo.toString());
        if(vo.getDdId()==0){
            logger.info("첫 배송조회 페이지 입장");
            vo.setDoneFlag(Integer.parseInt(senums.notFlag.get()));
            vo.setRoomId(roomId);
            vo.setUserId(userId);
            vo.setUserSocketId(userSocketId);
            deliverRoomDetailDao.save(vo);
            return;
        }else{
            //새로고침,페이지이동이 일어나서 소켓 아이디가 달라졌을경우
            if(!vo.getUserSocketId().equals(userSocketId)){
                logger.info("웹세션이 변경되었습니다");
                vo.setUserSocketId(userSocketId);
                return;
            }
        }
        logger.info("첫입장도 아니고 웹세션도 그대로임");
    }*/
}
