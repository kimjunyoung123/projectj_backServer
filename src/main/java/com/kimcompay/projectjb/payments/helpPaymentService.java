package com.kimcompay.projectjb.payments;

import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.service.basketService;
import com.kimcompay.projectjb.payments.service.couponService;

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
    private basketService basketService;

    //@Transactional(rollbackFor = Exception.class)//최상위 함수에 있음
    public boolean confrimPaymentAndInsert(String mchtTrdNo,int paymentPrice) {
        try {
            paymentVo vo2=getPaymentVoInRedis(mchtTrdNo);
            if(vo2.getTotalPrice()!=paymentPrice){
                throw utillService.makeRuntimeEX("총액 불일치", "confirmPayment");
            }
            List<Object>orders=getOrdersVoInRedis(mchtTrdNo);
            //쿠폰 사용처리
            for(Object order:orders){
                //쿠폰 사용처리
                ObjectMapper objectMapper = new ObjectMapper();
                orderVo orderVo=objectMapper.convertValue(order ,orderVo.class);
                String coupons=orderVo.getCoupon();
                if(coupons!=null){
                    String[] couponArr=orderVo.getCoupon().split(",");
                    for(String coupon:couponArr){
                        couponService.changeState(1, coupon, mchtTrdNo);
                    }
                }               
                orderDao.save(orderVo);
                //basketService.deleteById(order.getBasketId());//테스트시 꺼놓기
            }
            return true;
        } catch (Exception e) {
            utillService.writeLog("결제 정보 검증 실패 이유: "+e.getMessage(), helpPaymentService.class);
        }finally{
            utillService.getHttpServletRequest().getSession().removeAttribute("orderIdAndTid");
            redisTemplate.delete(mchtTrdNo);
        }
        return false;
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
}
