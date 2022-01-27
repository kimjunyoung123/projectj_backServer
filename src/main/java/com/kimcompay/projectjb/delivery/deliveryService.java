package com.kimcompay.projectjb.delivery;

import java.util.List;
import java.util.Optional;

import com.kimcompay.projectjb.delivery.model.deliverRoomDetailDao;
import com.kimcompay.projectjb.delivery.model.deliverRoomDetailVo;
import com.kimcompay.projectjb.delivery.model.deliveryRoomDao;
import com.kimcompay.projectjb.delivery.model.deliveryRoomVo;
import com.kimcompay.projectjb.enums.senums;

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

    public List<Integer> selectRoomIdByUserIdAndFlag(int userId,int flag) {
        logger.info("selectRoomIdByUserIdAndFlag");
        return deliverRoomDetailDao.findAllByRoomIdAndDoneFlag(userId, flag);
    }
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
        //배송요청조회 로직추가해야함 
        //배송방정보조회 null=첫배송조회 페이지 입장인경우 빈객체 생성
        logger.info(roomId+""+userId);
        deliverRoomDetailVo vo=deliverRoomDetailDao.findByFlagAndUserId(roomId,0, userId).orElseGet(()->new deliverRoomDetailVo());
        //logger.info(vo.toString());
        if(vo.getDdId()==0){
            logger.info("첫 배송조회 페이지 입장");
            vo.setDoneFlag(Integer.parseInt(senums.notFlag.get()));
            vo.setRoomId(roomId);
            vo.setUserID(userId);
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
    }
}
