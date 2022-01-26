package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth/user")
public class userAuthRestController {
    
    private Logger logger=LoggerFactory.getLogger(userRestController.class);

    @Autowired
    private userService userService;

    @RequestMapping(value = "/{action}",method = RequestMethod.GET)
    public JSONObject userAction(@PathVariable String action,HttpServletRequest request) {
        logger.info("userAction controller");
        return userService.getAuthActionHub(action,request);
    }
}
