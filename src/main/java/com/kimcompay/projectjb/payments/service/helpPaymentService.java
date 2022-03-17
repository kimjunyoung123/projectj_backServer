package com.kimcompay.projectjb.payments.service;

import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.exceptions.paymentFailException;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentDao;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class helpPaymentService {
    
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;
    @Autowired
    private couponService couponService;
    @Autowired
    private orderDao orderDao;
    @Autowired
    private paymentDao paymentDao;
    @Autowired
    private basketService basketService;

    //@Transactional(rollbackFor = Exception.class)//최상위 함수에 있음
    public void confrimPaymentAndInsert(String mchtTrdNo,int paymentPrice) {

            paymentVo vo2=getPaymentVoInRedis(mchtTrdNo);
            if(vo2.getTotalPrice()!=paymentPrice){
                throw utillService.makeRuntimeEX("총액 불일치", "confirmPayment");
            }
            List<Object>orders=getOrdersVoInRedis(mchtTrdNo);
            //쿠폰 사용처리
            for(Object order:orders){
                //쿠폰 사용처리
                orderVo orderVo=new ObjectMapper().convertValue(order ,orderVo.class);
                String coupons=orderVo.getCoupon();
                if(coupons!=null){
                    String[] couponArr=orderVo.getCoupon().split(",");
                    for(String coupon:couponArr){
                        couponService.changeState(1, coupon, mchtTrdNo);
                    }
                }               
                orderDao.save(orderVo);
                paymentDao.save(vo2);
                //basketService.deleteById(order.getBasketId());//테스트시 꺼놓기
            }
            removeInRedis(mchtTrdNo);
            utillService.getHttpServletRequest().getSession().removeAttribute("orderIdAndTid");
        throw new RuntimeException("test");
  
    }
    public paymentVo getPaymentVoInRedis(String mchtTrdNo) {
        LinkedHashMap<String,Object> tempPaymentInfor=(LinkedHashMap)redisTemplate.opsForHash().entries(mchtTrdNo);
        utillService.writeLog("임시 결제 요청정보: "+tempPaymentInfor.toString(), helpPaymentService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(tempPaymentInfor.get(mchtTrdNo) ,paymentVo.class);
    }
    public List<Object> getOrdersVoInRedis(String mchtTrdNo) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(redisTemplate.opsForHash().entries(mchtTrdNo+senums.basketsTextReids.get()).get(mchtTrdNo+senums.basketsTextReids.get()) ,List.class);
    }
    public void removeInRedis(String mchtTrdNo) {
        redisTemplate.delete(mchtTrdNo);
    }

}
