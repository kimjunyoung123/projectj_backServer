package com.kimcompay.projectjb.apis.aws.services;

import java.util.HashMap;
import java.util.Map;


import com.kimcompay.projectjb.apis.utillService;
import com.kimcompay.projectjb.apis.sns.emailService;
import com.kimcompay.projectjb.apis.sns.smsService;
import com.kimcompay.projectjb.enums.senums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

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
        Map<String,String>map=getMessageAndAddress(message);
        //smsService.sendMessege(map.get("val"),map.get("title")+"\n"+map.get("message"));
    }
    @SqsListener("projectj_email_sqs")
    public void loadEmailMessage(String message) {
        logger.info("email_sqs");
        logger.info("message: "+message);
        Map<String,String> map=getMessageAndAddress(message);
        emailService.sendEmail(map.get("val"),map.get("title"),map.get("message"));
    }
    public void sendSqs(String text,String type,String val) {
        logger.info("sendSqs");
        String end_point="https://sqs.ap-northeast-2.amazonaws.com/527222691614/";
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
        queueMessagingTemplate.send(end_point,MessageBuilder.withPayload(text+"/"+val).build());
        
    }
    private Map<String,String> getMessageAndAddress(String sqsMessage){
        logger.info("getMessageAndAddress");
        String[] strings=sqsMessage.split("/");
        sqsMessage=sqsMessage.replace("/"+strings[strings.length-1],"");
        Map<String,String>map=new HashMap<>();
        map.put("message", sqsMessage);
        map.put("val", strings[strings.length-1]);
        map.put("title", "안녕하세요 장보고 입니다");
        return map;
    }
}
