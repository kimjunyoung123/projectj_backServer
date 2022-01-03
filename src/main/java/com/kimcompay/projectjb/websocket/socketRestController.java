package com.kimcompay.projectjb.websocket;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class socketRestController {
    private Logger logger=LoggerFactory.getLogger(socketRestController.class);

    @MessageMapping("/recive")
    @SendTo("/send")
    public JSONObject test(@RequestBody JSONObject jsonObject) {
        logger.info("test");
        jsonObject.put("test", "value");
        return jsonObject;
    }
}
