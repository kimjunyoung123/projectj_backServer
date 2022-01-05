package com.kimcompay.projectjb.configs;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Service
public class textHandler extends TextWebSocketHandler {
    private Logger logger=LoggerFactory.getLogger(textHandler.class);
    Map<String, WebSocketSession> socketSessions = new HashMap<>(); 

    @Override//메세지가오는함수
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      logger.info("handleTextMessage");
      logger.info(message.toString());
      logger.info(session.toString());
      for(Entry<String, WebSocketSession> webSession:socketSessions.entrySet()){
         try {
            WebSocketSession socketSession=webSession.getValue();
            JSONObject whoAreYou=new JSONObject();
            whoAreYou.put("id", socketSession.getId());
            whoAreYou.put("message", message);
            webSession.getValue().sendMessage(new TextMessage(whoAreYou.toString()));
         } catch (IllegalStateException e) {
            e.printStackTrace();
            logger.info("닫힌 소켓 무시");
         }
      }
    }
   @Override//연결이되면 자동으로 작동하는함수
   public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      logger.info("afterConnectionEstablished");
      String id=session.getId();
      logger.info("소켓 연결 아이디: "+id);
      socketSessions.put(id, session);
   }
   @Override //연결이끊기면 자동으로 작동하는함수
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
      logger.info("afterConnectionClosed");
   }
}
