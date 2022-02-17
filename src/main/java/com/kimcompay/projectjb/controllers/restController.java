package com.kimcompay.projectjb.controllers;




import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.checkPageService;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.sns.snsService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.user.userService;


import org.json.simple.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
public class restController {
    @Autowired
    private snsService snsService;
    @Autowired
    private userService userService;
    @Autowired
    private checkPageService checkPageService;
    @Autowired
    private fileService fileService;

    
    @RequestMapping(value = "/sns/**",method = RequestMethod.POST)
    public JSONObject sendSns(@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        return snsService.send(jsonObject, httpSession);
    }
    @RequestMapping(value = "/confrim/{scope}/**",method = RequestMethod.POST)
    public JSONObject checkConfrim(@PathVariable String scope,@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        return snsService.confrim(jsonObject,httpSession);
    }
    @RequestMapping(value = "/checkPage/{scope}",method = RequestMethod.GET)
    public JSONObject checkPage(@PathVariable String scope,HttpServletRequest request) {
        return checkPageService.checkPage(request, scope);
      
    }
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JSONObject tryLogin(HttpServletRequest request,HttpServletResponse response) {
        return userService.checkLogin(request, response);
    }
    @RequestMapping(value = "/social/{kind}/{action}",method = RequestMethod.GET)
    public JSONObject soLogin(@PathVariable String kind,@PathVariable String action) {
        return checkPageService.selectPage(kind,action);
    }
    @RequestMapping(value = "/callback/{kind}/{action}",method = RequestMethod.GET)
    public void socialCallback(@PathVariable String kind,@PathVariable String action,HttpServletRequest request) {
        checkPageService.selectCallback(kind,action,request);
    }
    @RequestMapping("/tokenExpire/{result}")
    public JSONObject tokenExpire(@PathVariable String result,HttpServletRequest request) {
        if(result.equals(senums.newToken.get())){
            return utillService.getJson(true, "new");
        }else{
            return utillService.getJson(false, "로그인이 만료되었습니다 다시 로그인 바랍니다");
        }
        
    }
    @RequestMapping(value = "/auth/file/{action}",method = RequestMethod.POST)
    public JSONObject imgController(@PathVariable String action,MultipartHttpServletRequest request) {
        return fileService.upload(request);
    }
}
