package com.kimcompay.projectjb.delivery;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailDao;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailVo;
import com.kimcompay.projectjb.delivery.model.deliveryRoomDao;
import com.kimcompay.projectjb.delivery.model.deliveryRoomVo;
import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class deliveryService {
    private Logger logger=LoggerFactory.getLogger(deliveryService.class);

    @Autowired
    private deliveryRoomDao deliveryRoomDao;
    @Autowired
    private deliverRoomDetailDao deliverRoomDetailDao;

    public JSONObject getDelivers(int page,String startDay,String endDay,int storeId) {
        logger.info("getDelivers");
        List<deliveryRoomVo>deliveryRoomVos=selectByPeriodAndStoreId(page, startDay, endDay, storeId);
        //본인 가게 배달방이 맞나 검사
        int loginId=utillService.getLoginId();
        for(deliveryRoomVo deliverRoom:deliveryRoomVos ){
            if(deliverRoom.getCompanyId()!=loginId){
                logger.info("다른 매장 배달 발견");
                throw utillService.makeRuntimeEX("회사 소유 매장 배달이 아닙니다", "getDelivers");
            }
        }
        //배달건수가 없을 수도있음 검사
        if(utillService.checkEmthy(deliveryRoomVos)){
            throw utillService.makeRuntimeEX("배달건수가 존재하지 않습니다", "getDelivers");
        }
        return utillService.getJson(true, deliveryRoomVos);
    }
    public List<deliveryRoomVo> selectByPeriodAndStoreId(int page,String startDay,String endDay,int storeId) {
        logger.info("getDelivers");
        Timestamp daystart=Timestamp.valueOf(startDay+" 00:00:00");
        Timestamp dayEnd=Timestamp.valueOf(endDay+" 23:59:59");
        
        return deliveryRoomDao.findByDay(daystart, dayEnd,storeId);
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
