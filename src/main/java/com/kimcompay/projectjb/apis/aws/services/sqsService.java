package com.kimcompay.projectjb.apis.aws.services;

import com.kimcompay.projectjb.apis.utillService;
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
    private String end_point="https://sqs.ap-northeast-2.amazonaws.com/527222691614/";
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    
    @SqsListener("sms_sqs")
    public void loadSMSMessage(String message) {
        logger.info("sms_sqs");
        logger.info("message: "+message);
    }
    @SqsListener("email_sqs")
    public void loadEmailMessage(String message) {
        logger.info("email_sqs");
        logger.info("message: "+message);
    }
    public void sendSqs(String text,String type) {
        logger.info("sendSqs");
        if(type.equals(senums.phonet.get())){
            logger.info("휴대폰 sqs전송시도");
            end_point+="sms_sqs";
        }else if(type.equals(senums.emailt.get())){
            logger.info("이메일 sqs전송시도");
            end_point+="email_sqs";
        }else{
            utillService.throwRuntimeEX("sqs 전송 실패 지원하지 않는 인증수단");
        }
        queueMessagingTemplate.send(end_point,MessageBuilder.withPayload(text).build());
        
    }
}
