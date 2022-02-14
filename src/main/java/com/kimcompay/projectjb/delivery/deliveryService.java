package com.kimcompay.projectjb.delivery;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailDao;
import com.kimcompay.projectjb.delivery.model.deliveryRoomDao;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class deliveryService {
    private Logger logger=LoggerFactory.getLogger(deliveryService.class);
    private final int pageSize=2;

    @Autowired
    private deliveryRoomDao deliveryRoomDao;
    @Autowired
    private deliverRoomDetailDao deliverRoomDetailDao;

    public JSONObject getDeliverAddress(int roomId) {
        logger.info("getDeliverAddress");
        return utillService.getJson(true, deliverRoomDetailDao.findAddressByRoomId(roomId));
    }
    public JSONObject getDelivers(int page,String startDay,String endDay,int storeId,int state) {
        logger.info("getDelivers");
        List<Map<String,Object>>deliveryInfors=selectByPeriodAndStoreId(page, startDay, endDay, storeId,state);
        //요청페이지,배달건수 검증
        if(utillService.checkEmthy(deliveryInfors)){ 
            throw utillService.makeRuntimeEX("배달건수가 존재하지 않습니다", "getDelivers");
        } 
        int totalPage=utillService.getTotalPage(Integer.parseInt(deliveryInfors.get(0).get("totalCount").toString()),pageSize);
        //본인 가게 배달방이 맞나 검사
        int loginId=utillService.getLoginId();
        for(Map<String,Object> deliveryInfor:deliveryInfors ){
            if(Integer.parseInt(deliveryInfor.get("company_id").toString())!=loginId){
                logger.info("다른 매장 배달 발견");
                throw utillService.makeRuntimeEX("회사 소유 매장 배달이 아닙니다", "getDelivers");
            }
        }
        JSONObject response =new JSONObject();
        response.put("flag", true);
        response.put("totalPage",  totalPage);
        response.put("message", deliveryInfors);
        return response;
    }
    public List<Map<String,Object>> selectByPeriodAndStoreId(int page,String startDay,String endDay,int storeId,int state) {
        logger.info("getDelivers");
        logger.info("조회날짜: "+startDay+", "+endDay);
        Map<String,Object>result=utillService.checkRequestDate(startDay, endDay);
        if((boolean)result.get("flag")){
            Timestamp start=Timestamp.valueOf(result.get("start").toString());
            Timestamp end=Timestamp.valueOf(result.get("end").toString());
            return deliveryRoomDao.findByDay(start, end, storeId,state,start, end, storeId,state,utillService.getStart(page, pageSize)-1,pageSize);

        }
        return deliveryRoomDao.findByAll(storeId,state,storeId,state,utillService.getStart(page, pageSize)-1,pageSize);
    }
    public List<Integer> selectRoomIdByUserIdAndFlag(int userId,int flag) {
        logger.info("selectRoomIdByUserIdAndFlag");
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
