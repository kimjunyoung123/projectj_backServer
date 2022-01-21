package com.kimcompay.projectjb.delivery;

import java.util.Optional;

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
    public void enterRoom(int roomId,int userId) {
        logger.info("enterRoom");
        deliveryRoomVo vo=deliveryRoomDao.findById(roomId).orElseThrow();
        String custermersIds=Optional.ofNullable(vo.getDeliverRoomCustomerIds()).orElseGet(()->"");
        if(custermersIds.equals("")){
            logger.info("첫손님");
            custermersIds+=userId+",";
        }else{
            custermersIds+=","+userId+",";
        }
        vo.setDeliverRoomCustomerIds(custermersIds);

    }
}
