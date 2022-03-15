package com.kimcompay.projectjb.apis.kakao;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.enums.kenum;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.payments.model.kpay.kpayDao;
import com.kimcompay.projectjb.payments.model.kpay.kpayVo;
import com.kimcompay.projectjb.payments.model.order.orderDao;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentDao;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;
import com.kimcompay.projectjb.payments.service.basketService;
import com.kimcompay.projectjb.payments.service.couponService;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class kakaoPayService {
    
    @Autowired
    private requestTo requestTo;
    @Autowired
    private kpayDao kpayDao;
    @Autowired
    private paymentDao paymentDao;
    @Autowired
    private orderDao orderDao;
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;
    @Autowired
    private couponService couponService;
    @Autowired
    private basketService basketService;
    
    @Value("${kakao.admin.key}")
    private String kakaoAdminKey;
    @Value("${kakao.pay.cid}")
    private String cid;
    @Value("${kakao.pay.approval_url}")
    private String approvalUrl;
    @Value("${kakao.pay.cancel_url}")
    private String cancleUrl;
    @Value("${kakao.pay.fail_url}")
    private String failUrl;



    public void tryInsert(int paymentId,String mchtTrdNo) {
        kpayVo vo=kpayVo.builder().mchtTrdNo(mchtTrdNo).paymentId(paymentId).build();
        kpayDao.save(vo);
    }
    public JSONObject requestPay(String productNames,paymentVo paymentVo,List<orderVo>orders,int totalCount) {
        String partnerOrderId= paymentVo.getMchtTrdNo();
        //header만들기
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "KakaoAK "+kakaoAdminKey);
        //body만들기
        MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
        multiValueBody.add("cid", cid);
        multiValueBody.add("partner_order_id",partnerOrderId);
        multiValueBody.add("partner_user_id", utillService.getLoginId());
        multiValueBody.add("item_name",productNames);
        multiValueBody.add("quantity", totalCount);
        multiValueBody.add("total_amount", paymentVo.getTotalPrice());
        multiValueBody.add("tax_free_amount", 0);
        multiValueBody.add("approval_url", approvalUrl);
        multiValueBody.add("cancel_url", cancleUrl);
        multiValueBody.add("fail_url", failUrl);
        JSONObject respon=requestTo.requestPost(multiValueBody, "https://kapi.kakao.com/v1/payment/ready", httpHeaders);
        utillService.writeLog("카카오페이 통신결과: "+respon.toString(), kakaoPayService.class);
        JSONObject urls=new JSONObject();
        urls.put("pc", respon.get("next_redirect_pc_url"));
        urls.put("mobile", respon.get("next_redirect_app_url"));
        urls.put("app", respon.get("next_redirect_app_url"));
        urls.put("flag", true);
        utillService.getHttpServletRequest().getSession().setAttribute("orderIdAndTid", partnerOrderId+","+respon.get("tid"));
        return urls;
    }
    @Transactional(rollbackFor = Exception.class)
    public JSONObject confirmPayment(HttpServletRequest request) {
        if(request.getParameter("state").equals("fail")){
            return utillService.getJson(false, "결제에 실패했습니다");
        }
        HttpSession httpSession=utillService.getHttpServletRequest().getSession();
        String[] orderIdAndTid=httpSession.getAttribute("orderIdAndTid").toString().split(",");
        String mchtTrdNo=orderIdAndTid[0];
        String tid= orderIdAndTid[1];
        //결제 요청시 저장했던 정보 꺼내기
        LinkedHashMap<String,Object> tempPaymentInfor=(LinkedHashMap)redisTemplate.opsForHash().entries(mchtTrdNo);
        utillService.writeLog("임시 결제 요청정보: "+tempPaymentInfor.toString(), kakaoPayService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        paymentVo vo2=objectMapper.convertValue(tempPaymentInfor.get(mchtTrdNo) ,paymentVo.class);
        try {
            String pgToken=request.getParameter("pg_token").toString();
            HttpHeaders httpHeaders=new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            httpHeaders.add("Authorization", "KakaoAK "+kakaoAdminKey);
            MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
            multiValueBody.add("cid", cid);
            multiValueBody.add("partner_order_id", mchtTrdNo);
            multiValueBody.add("partner_user_id", utillService.getLoginId());
            multiValueBody.add("tid", tid);
            multiValueBody.add("pg_token", pgToken);
            JSONObject  respon=requestTo.requestPost(multiValueBody, "https://kapi.kakao.com/v1/payment/approve ", httpHeaders);
            utillService.writeLog("카카오페이 통신결과: "+respon.toString(), kakaoPayService.class);
            //결제금액 비교
            Map<String,Object>amount=(Map<String, Object>) respon.get("amount");
            if(vo2.getTotalPrice()!=Integer.parseInt(amount.get("total").toString())){
                throw utillService.makeRuntimeEX("카카오페이 총액 불일치", "confirmPayment");
            }
            //main db insert
            List<Object>orders=objectMapper.convertValue(redisTemplate.opsForHash().entries(mchtTrdNo+senums.basketsTextReids.get()).get(mchtTrdNo+senums.basketsTextReids.get()) ,List.class);
            kpayVo vo=kpayVo.builder().mchtTrdNo(mchtTrdNo).paymentId(utillService.getLoginId()).tid(tid).build();
            paymentDao.save(vo2);
            kpayDao.save(vo);
            for(Object order:orders){
                orderVo orderVo=objectMapper.convertValue(order, orderVo.class);
                //쿠폰 사용처리
                String coupons=orderVo.getCoupon();
                System.out.println(coupons);
                if(coupons!=null){
                    String[] couponArr=orderVo.getCoupon().split(",");
                    for(String coupon:couponArr){
                        couponService.changeState(1, coupon, mchtTrdNo);
                    }
                }               
                orderDao.save(orderVo);
                basketService.deleteById(orderVo.getBasketId());
            }
            return utillService.getJson(true, "결제가 완료 되었습니다");
            //throw new RuntimeException("test");
        } catch (Exception e) {
            e.printStackTrace();
            utillService.writeLog("카카오페이 결제 실패", kakaoPayService.class);
            String message="카카오 페이 결제에 실패하였습니다  \n전액 환불되었습니다";
            if(!cancleKpay(tid, vo2.getTotalPrice(),0)){
                message="카카오 페이 결제에 실패하였습니다\n 환불에 실패하였습니다\n 관리자에게 문의주세요";
            }
            throw utillService.makeRuntimeEX(message,message);
        }finally{
            //실패하든 성공하든 지워주기
            httpSession.removeAttribute("orderIdAndTid");
            redisTemplate.delete(mchtTrdNo);
        }
    }
    private boolean cancleKpay(String tid,int cancleAmount,int taxFree) {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "KakaoAK "+kakaoAdminKey);
        MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
        multiValueBody.add("cid", cid);
        multiValueBody.add("cancel_amount", cancleAmount);
        multiValueBody.add("partner_user_id", utillService.getLoginId());
        multiValueBody.add("tid", tid);
        multiValueBody.add("cancel_tax_free_amount", taxFree);
        try {
            JSONObject  respon=requestTo.requestPost(multiValueBody, "https://kapi.kakao.com/v1/payment/cancel ", httpHeaders);
            utillService.writeLog("카카오페이 통신결과: "+respon.toString(), kakaoPayService.class);
            return true;
        } catch (Exception e) {
            utillService.writeLog("카카오페이 환불실패 사유: "+e.getMessage(), kakaoPayService.class);
        }
        return false;
    }
}
