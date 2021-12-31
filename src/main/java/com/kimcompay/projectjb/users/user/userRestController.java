package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.kimcompay.projectjb.users.user.model.tryInsertDto;
import com.kimcompay.projectjb.users.user.model.tryUpdatePwdDato;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class userRestController {
    private Logger logger=LoggerFactory.getLogger(userRestController.class);
    
    @Autowired
    private userService userService;

    @RequestMapping(value = "/auth/user/{action}",method = RequestMethod.GET)
    public JSONObject userAction(@PathVariable String action,HttpServletRequest request) {
        logger.info("userAction controller");
        return userService.getAuthActionHub(action,request);
    }
    @RequestMapping(value = "/user/{action}",method = RequestMethod.GET)
    public JSONObject userActionNotLogin(@PathVariable String action,HttpServletRequest request) {
        logger.info("userActionNotLogin controller");
        return userService.getActionHub(action,request);
    }
    @RequestMapping(value = "/user/join",method = RequestMethod.POST)
    public JSONObject tryJoin(@Valid @RequestBody tryInsertDto tryInsertDto ,HttpSession session) {
        logger.info("tryJoin");
        logger.info(tryInsertDto.toString());
        return userService.insert(tryInsertDto, session);
    }
    @RequestMapping(value = "/user/change/pwd",method = RequestMethod.PUT)
    public JSONObject tryChangeUserInfor(@Valid @RequestBody tryUpdatePwdDato tryUpdatePwdDato) {
        logger.info("tryChangeUserInfor controller");
        return userService.changePwdForLost(tryUpdatePwdDato);
    }
}
