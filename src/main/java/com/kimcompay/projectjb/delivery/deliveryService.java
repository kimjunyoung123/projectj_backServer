package com.kimcompay.projectjb.delivery;

import java.util.Optional;

import com.kimcompay.projectjb.delivery.model.deliverRoomDetailDao;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailVo;
import com.kimcompay.projectjb.delivery.model.deliveryRoomDao;
import com.kimcompay.projectjb.delivery.model.deliveryRoomVo;

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

    public void makeDeliverRoom(int compayId) {
        logger.info("makeDeliverRoom");
        deliveryRoomVo vo=new deliveryRoomVo();
        vo.setDeliverRoomMaster(compayId);
        deliveryRoomDao.save(vo);
    }
    public int checkAlreadyRoom(int companyId) {
        logger.info("checkAlreadyRoom");
        return deliveryRoomDao.findCountByRoomMaster(companyId);
    }
    @Transactional(rollbackFor = Exception.class)
    public void enterRoom(int roomId,String userSocketId,int userId) {
        logger.info("enterRoom");
        deliverRoomDetailVo deliverRoomDetailVo=deliverRoomDetailDao.findByRoomIdAndUserId(roomId, userId);
        logger.info(deliverRoomDetailVo.toString());
        if(!Optional.ofNullable(deliverRoomDetailVo.getUserSocketId()).orElseGet(()->"").equals(userSocketId)){
            deliverRoomDetailVo.setUserSocketId(userSocketId);
        }
    }
    public void findAllByRoomId(int roomId) {
        logger.info("findAllByRoomId");
        
    }
}
