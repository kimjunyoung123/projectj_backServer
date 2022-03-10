package com.kimcompay.projectjb.apis.kakao;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.requestTo;
import com.kimcompay.projectjb.payments.model.kpay.kpayDao;
import com.kimcompay.projectjb.payments.model.kpay.kpayVo;
import com.kimcompay.projectjb.payments.model.order.orderVo;
import com.kimcompay.projectjb.payments.model.pay.paymentVo;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class kakaoPayService {
    
    @Autowired
    private requestTo requestTo;
    @Autowired
    private kpayDao kpayDao;
    @Autowired
    private RedisTemplate<String,Object>redisTemplate;
    
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
    public JSONObject confirmPayment(HttpServletRequest request) {
        if(request.getParameter("state").equals("fail")){
            return utillService.getJson(false, "결제에 실패했습니다");
        }
        HttpSession httpSession=utillService.getHttpServletRequest().getSession();
        String[] orderIdAndTid=httpSession.getAttribute("orderIdAndTid").toString().split(",");
        String pgToken=request.getParameter("pg_token").toString();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.add("Authorization", "KakaoAK "+kakaoAdminKey);
        MultiValueMap<String,Object> multiValueBody=new LinkedMultiValueMap<>();
        multiValueBody.add("cid", cid);
        multiValueBody.add("partner_order_id", orderIdAndTid[0]);
        multiValueBody.add("partner_user_id", utillService.getLoginId());
        multiValueBody.add("tid", orderIdAndTid[1]);
        multiValueBody.add("pg_token", pgToken);
        JSONObject  respon=requestTo.requestPost(multiValueBody, "https://kapi.kakao.com/v1/payment/approve ", httpHeaders);
        utillService.writeLog("카카오페이 통신결과: "+respon.toString(), kakaoPayService.class);
        return null;
    }
}
