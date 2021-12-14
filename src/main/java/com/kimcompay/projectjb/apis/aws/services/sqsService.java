package com.kimcompay.projectjb.apis.aws.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

@Service
public class sqsService {
    private Logger logger=LoggerFactory.getLogger(sqsService.class);
    
    @SqsListener("testsqs")
    public void loadMessage(String message,String kind) {
        logger.info("message: "+message);
    }
}
