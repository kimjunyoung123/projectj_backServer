package com.kimcompay.projectjb.controllers;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.sns.snsService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.jwt.jwtService;
import com.kimcompay.projectjb.users.user.tryInsertDto;
import com.kimcompay.projectjb.users.user.userService;
import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class restController {
    private Logger logger=LoggerFactory.getLogger(restController.class);
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private snsService snsService;
    @Autowired
    private userService userService;

    //
    @RequestMapping(value = "/api/test",method = RequestMethod.GET)
    public void name(HttpSession session) {
        logger.info("test");
    }
    @RequestMapping(value = "/message",method =RequestMethod.POST )
    public void sendSqs(HttpServletRequest request,HttpServletResponse response) {
        logger.info("sendSqs");
        String url="https://sqs.ap-northeast-2.amazonaws.com/527222691614/testsqs";
        String message=request.getParameter("message");
        queueMessagingTemplate.send(url,MessageBuilder.withPayload(message).build());
        
    }
    @RequestMapping(value = "/sns/**",method = RequestMethod.POST)
    public JSONObject sendSns(@RequestBody JSONObject jsonObject,HttpSession httpSession,HttpServletResponse response) {
        logger.info("sendSns Controller");
        return snsService.send(jsonObject, httpSession);
    }
    @RequestMapping(value = "/confrim/{scope}/**",method = RequestMethod.POST)
    public JSONObject checkConfrim(@PathVariable String scope,@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        logger.info("checkConfrim");
        if(scope.equals(senums.auth.get())){
            return snsService.confrim(jsonObject,httpSession);
        }else if(scope.equals("change")){
             return userService.findChangePwdToken(jsonObject.get("val").toString());
        }
        return utillService.getJson(false, "잘못된 요청입니다");
    }
    @RequestMapping(value = "/user/**",method = RequestMethod.POST)
    public JSONObject tryJoin(@Valid @RequestBody tryInsertDto tryInsertDto ,HttpSession session) {
        logger.info("tryJoin");
        logger.info(tryInsertDto.toString());
        return userService.insert(tryInsertDto, session);
    }
    @RequestMapping(value = "/login/{scope}/{detail}",method = RequestMethod.POST)
    public JSONObject tryLogin(@PathVariable String detail,@PathVariable String scope,HttpServletRequest request,HttpServletResponse response) {
        logger.info("tryLogin");
        try {
            if(scope.equals(senums.logint.get())){
                return userService.checkLogin(request, response);
            }else if(scope.equals(senums.checkt.get())){
                return userService.checkLogin(request,detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return utillService.getJson(false, "존재 하지 않는 처리");
    }
    @RequestMapping("/tokenExpire/{result}")
    public JSONObject tokenExpire(@PathVariable String result,HttpServletRequest request,HttpServletResponse response) {
        logger.info("tokenExpire controller");
        if(result.equals(senums.newToken.get())){
            return utillService.getJson(true, "new");
        }else{
            return utillService.getJson(false, "로그인이 만료되었습니다 다시 로그인 바랍니다");
        }
        
    }
}
