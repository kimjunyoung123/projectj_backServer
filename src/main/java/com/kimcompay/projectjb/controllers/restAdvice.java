package com.kimcompay.projectjb.controllers;

import java.util.ArrayList;
import java.util.List;

import com.kimcompay.projectjb.apis.utillService;
import com.nimbusds.jose.shaded.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class restAdvice {

    private final static Logger LOGGER=LoggerFactory.getLogger(restAdvice.class);

    @ExceptionHandler(RuntimeException.class)
    public JSONObject runtimeException(RuntimeException exception) {
        LOGGER.info("runtimeException");
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
        LOGGER.info("processValidationError 유효성 검사 실패");
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
