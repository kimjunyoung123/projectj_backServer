package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpServletRequest;

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
    private userService userService;

    @RequestMapping(value = "/{action}",method = RequestMethod.GET)//매 페이지 이동 마다 로그인정보 조회
    public JSONObject userAction(@PathVariable String action,HttpServletRequest request) {
        return userService.getAuthActionHub(action,request);
    }
    //매장 리뷰 등록
    @RequestMapping(value = "/review",method = RequestMethod.POST)
    public void tryInsertReview(@RequestBody JSONObject jsonObject) {
        System.out.println(jsonObject.toString());
    }
    //매장 리뷰 수정
    @RequestMapping(value = "/review",method = RequestMethod.PUT)
    public void tryPutReview(@RequestBody JSONObject jsonObject) {
        System.out.println(jsonObject.toString());
    }    
}
