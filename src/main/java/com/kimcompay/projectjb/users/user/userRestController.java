package com.kimcompay.projectjb.users.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.kimcompay.projectjb.users.user.model.tryInsertDto;
import com.kimcompay.projectjb.users.user.model.tryUpdatePwdDato;

import org.json.simple.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class userRestController {
    
    @Autowired
    private userService userService;

    @RequestMapping(value = "/{action}",method = RequestMethod.GET)//user/get요청오는거 다받음
    public JSONObject userActionNotLogin(@PathVariable String action,HttpServletRequest request) {
        return userService.getActionHub(action,request);
    }
    @RequestMapping(value = "/join",method = RequestMethod.POST)//회원가입 
    public JSONObject tryJoin(@Valid @RequestBody tryInsertDto tryInsertDto ,HttpSession session) {
        return userService.insert(tryInsertDto, session);
    }
    @RequestMapping(value = "/change/pwd",method = RequestMethod.PUT)//비밀번호 찾기 후 변경
    public JSONObject tryChangeUserInfor(@Valid @RequestBody tryUpdatePwdDato tryUpdatePwdDato) {
        return userService.changePwdForLost(tryUpdatePwdDato);
    }
}
