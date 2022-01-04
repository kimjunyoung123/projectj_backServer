package com.kimcompay.projectjb.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component
public class textHandler extends TextWebSocketHandler {
    private Logger logger=LoggerFactory.getLogger(textHandler.class);
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
       logger.info("handleTextMessage");
       logger.info(message.toString());
       logger.info(session.toString());
    }

   // connection established
   @Override
   public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    logger.info("afterConnectionEstablished");
   }

   // connection closed
   @Override
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
   
   }
}
