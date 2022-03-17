package com.kimcompay.projectjb.apis.kakao;



import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.enums.kenum;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.exceptions.socialFailException;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class kakaoService {    
    @Value("${kakao.app.key}")
    private String app_key;
    @Value("${kakao.rest.key}")
    private String rest_key;
    @Value("${kakao.js.key}")
    private String js_key;
    @Value("${kakao.admin.key}")
    private String admin_key;
    @Value("${k_login_callback_url}")
    private String kLoginCallbackUrl;
    @Value("${front.domain}")
    private String frontDomain;
    @Value("${front.result.page}")
    private String resultLink;

    @Autowired
    private kakaoLoginService kakaoLoginService;
    @Autowired
    private kakaoPayService kakaoPayService;

    public JSONObject callPage(String action) {
        String url=null;
        if(action.equals(kenum.loginPage.get())){
            url="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="+rest_key+"&redirect_uri="+kLoginCallbackUrl;
        }else{
            throw utillService.makeRuntimeEX("지원하지 않는 카카오페이지 입니다", "callPage");
        }
        return utillService.getJson(true, url);
    }
    public void catchCallBack(String action,HttpServletRequest request) throws socialFailException {
        JSONObject result=new JSONObject();
        if(action.equals(kenum.loginPage.get())){
            result=kakaoLoginService.doLogin(request.getParameter("code"),rest_key,kLoginCallbackUrl);
        }else if(action.equals(senums.paymentText.get())){
            System.out.println("카카오페이 콜백");
            result=kakaoPayService.confirmPayment(request);
            System.out.println(result.toString());
        }else{
            result.put("flag", false);
            result.put("message", senums.defaultDetailAddress.get());
        }
        String url=frontDomain+resultLink+"?kind=kakao&action="+action+"&result="+result.get("flag")+"&message="+result.get("message");
        utillService.doRedirect(utillService.getHttpSerResponse(), url);
    }
    public String failToAction(Object body,String action) {
        try {
            if(action.equals(senums.paymentText.get())){
                JSONObject reponse=utillService.changeClass(body, JSONObject.class);
                Map<String,Object>amount=(Map<String, Object>) reponse.get("amount");
                Boolean result=kakaoPayService.cancleKpay(reponse.get("tid").toString(), Integer.parseInt(amount.get("total").toString()),Integer.parseInt(amount.get("tax_free").toString()));
                if(result){
                    return "메세지:카카오 페이 결제에 실패하였습니다$전액 환불되었습니다";
                }
                return "메세지:카카오 페이 결제에 실패하였습니다$환불에 실패하였습니다$관리자에게 문의주세요";
            }
        } catch (Exception e) {
            
        }
        return "메세지:카카오 예외처리에 실패했습니다$관리자에게 문의해주세요";
    }
}
