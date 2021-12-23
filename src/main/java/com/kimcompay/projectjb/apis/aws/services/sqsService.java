package com.kimcompay.projectjb.apis.aws.services;

import java.util.HashMap;
import java.util.Map;

import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.sns.emailService;
import com.kimcompay.projectjb.apis.sns.smsService;
import com.kimcompay.projectjb.enums.senums;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;


@Service
public class sqsService {
    private Logger logger=LoggerFactory.getLogger(sqsService.class);
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private smsService smsService;
    @Autowired
    private emailService emailService;

    @SqsListener("projectj_sms_sqs")
    public void loadSMSMessage(String message) {
        logger.info("sms_sqs");
        logger.info("message: "+message);
        JSONObject titleAndTextAndAddress=getMessageAndAddress(message);
        smsService.sendMessege(titleAndTextAndAddress.get("val").toString(),titleAndTextAndAddress.get("title")+"\n"+titleAndTextAndAddress.get("text"));//문자발송은실제로돈이나가서 막아논것
    }
    @SqsListener("projectj_email_sqs")
    public void loadEmailMessage(String message) {
        logger.info("email_sqs");
        logger.info("message: "+message);
        JSONObject titleAndTextAndAddress=getMessageAndAddress(message);
        emailService.sendEmail(titleAndTextAndAddress.get("val").toString(),titleAndTextAndAddress.get("title").toString(),titleAndTextAndAddress.get("text").toString());
    }
    public void sendSqs(String title,String text,String type,String val) {
        logger.info("sendSqs");
        String end_point=senums.sqsEndPoint.get();
        if(type.equals(senums.phonet.get())){
            logger.info("휴대폰 sqs전송시도");
            end_point+="projectj_sms_sqs";
        }else if(type.equals(senums.emailt.get())){
            logger.info("이메일 sqs전송시도");
            end_point+="projectj_email_sqs";
        }else{
            utillService.throwRuntimeEX("sqs 전송 실패 지원하지 않는 인증수단");
        }
        logger.info("sqs주소: "+end_point);
        //구성만들기
        queueMessagingTemplate.send(end_point,MessageBuilder.withPayload(makeTitleAndText(title,text,val)).build());
        
    }
    private JSONObject getMessageAndAddress(String sqsMessage){
        logger.info("getMessageAndAddress");
        logger.info("메세지내용: "+sqsMessage);
        return utillService.stringToJson(sqsMessage);
    }
    private JSONObject makeTitleAndText(String title,String text,String val) {
        logger.info("makeTitleAndText");
        logger.info("제목: "+title+",내용: "+text+",받는주소/번호: "+val);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("text", text);
        jsonObject.put("val", val);
        return jsonObject;
    }
    @Async
    public ListenableFuture<String> sendEmailAsync(String text,String val) {
        logger.info("sendEmailAsync");
        String end_point=senums.sqsEndPoint.get();
        end_point+="projectj_email_sqs";
        logger.info("sqs주소: "+end_point);
        queueMessagingTemplate.send(end_point,MessageBuilder.withPayload(makeTitleAndText("안녕하세요 장보고입니다", text, val)).build());
        return new AsyncResult<>("결과");
    }
    @Async
    public ListenableFuture<String> sendPhoneAsync(String text,String val) {
        logger.info("sendEmailAsync");
        String end_point=senums.sqsEndPoint.get();
        end_point+="projectj_sms_sqs";
        logger.info("sqs주소: "+end_point);
        queueMessagingTemplate.send(end_point,MessageBuilder.withPayload(makeTitleAndText("안녕하세요 장보고입니다", text, val)).build());
        return new AsyncResult<>("결과");
    }
    
}
