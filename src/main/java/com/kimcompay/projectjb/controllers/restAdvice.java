package com.kimcompay.projectjb.controllers;

import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.kakao.kakaoPayService;
import com.kimcompay.projectjb.apis.kakao.kakaoService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.exceptions.paymentFailException;
import com.kimcompay.projectjb.exceptions.socialFailException;

import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class restAdvice {
    private final static Logger logger=LoggerFactory.getLogger(restAdvice.class);
    @Autowired
    private kakaoService kakaoService;

    @ExceptionHandler(socialFailException.class)
    public void socialFailException(socialFailException exception) {
        logger.info("socialFailException");
        String message=exception.getMessage();
        String comapany=exception.getCompany();
        String action= exception.getAction();
        if(comapany.equals(senums.kakao.get())){
            message=kakaoService.failToAction(exception.getBody(),action);
        }
        if(!message.startsWith("메")){
            message="알수 없는 오류발생";
            exception.printStackTrace();
        }
        utillService.redirectToResultPage(senums.kakao.get(),action, false, message);
    }
    @ExceptionHandler(paymentFailException.class)
    public JSONObject paymentFailException(paymentFailException exception) {
        logger.info("paymentFailException");
        String message=exception.getMessage();
        if(!message.startsWith("메")){
            message="알수 없는 오류발생";
            exception.printStackTrace();
        }
        return utillService.getJson(false, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public JSONObject runtimeException(RuntimeException exception) {
        logger.info("runtimeException");
        String message=exception.getMessage();
        if(!message.startsWith("메")){
            message="알수 없는 오류발생";
            exception.printStackTrace();
        }
        //exception.printStackTrace();
        return utillService.getJson(false, message);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public JSONObject processValidationError(MethodArgumentNotValidException exception) {
        logger.info("processValidationError 유효성 검사 실패");
        BindingResult bindingResult = exception.getBindingResult();
        StringBuilder builder = new StringBuilder();
        List<String>list=new ArrayList<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append(fieldError.getDefaultMessage());
            list.add(fieldError.getField());
        }
        return utillService.getJson(false, builder.toString());
    }
}
