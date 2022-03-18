package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.payments.model.basket.tryInsertDto;
import com.kimcompay.projectjb.payments.service.basketService;
import com.kimcompay.projectjb.payments.service.paymentService;
import com.kimcompay.projectjb.users.user.service.reviewService;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth/user")
public class userAuthRestController {
    
    @Autowired
    private reviewService reviewService;
    @Autowired
    private userService userService;
    @Autowired
    private basketService basketService;
    @Autowired
    private paymentService paymentService;

    @RequestMapping(value = "/{action}",method = RequestMethod.GET)//매 페이지 이동 마다 로그인정보 조회
    public JSONObject userAction(@PathVariable String action,HttpServletRequest request) {
        return userService.getAuthActionHub(action,request);
    }
    //매장 리뷰 등록
    @RequestMapping(value = "/review/{action}/{id}",method = {RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
    public JSONObject tryInsertReview(@RequestBody(required = false) JSONObject jsonObject,@PathVariable int id,@PathVariable String action) {
        if(action.equals("insert")){
            return reviewService.tryInsert(jsonObject,id);
        }else if(action.equals("update")){
            return reviewService.tryUpadte(jsonObject, id);
        }else if(action.equals("delete")){
            return reviewService.tryDelete(id);
        }else{
            return utillService.getJson(false, "유효하지 않는 요청입니다");
        }
    }  
    //장바구니 등록
    @RequestMapping(value = "/basket",method = RequestMethod.POST)
    public JSONObject tryInsertBaket(@Valid @RequestBody tryInsertDto tryInsertDto) {
        return basketService.tryInsert(tryInsertDto);
    } 
    //장바구니 불러오기
    @RequestMapping(value = "/baskets/{page}",method = RequestMethod.GET)
    public JSONObject tryInsertBaket(@PathVariable int page) {
        return basketService.getBaskets(page);
    } 
    //장바구니 수정
    @RequestMapping(value = "/basket/{basketId}/{count}",method = RequestMethod.PUT)
    public JSONObject tryInsertBaket(@PathVariable int basketId,@PathVariable int count) {
        return basketService.tryUpadte(basketId, count);
    } 
    //장바구니 삭제
    @RequestMapping(value = "/basket/{basketId}",method = RequestMethod.DELETE)
    public JSONObject tryDeleteBaket(@PathVariable int basketId) {
        return basketService.tryDelete(basketId);
    }
    //주문조회
    @RequestMapping(value = "/payments/{page}/{start}/{end}",method = RequestMethod.GET)
    public JSONObject getPaymentList(@PathVariable int page,@PathVariable String start,@PathVariable String end) {
        return paymentService.getPayments(page,start,end);
    }
}
