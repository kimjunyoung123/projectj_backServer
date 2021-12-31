package com.kimcompay.projectjb.controllers;




import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.kimcompay.projectjb.checkPageService;
import com.kimcompay.projectjb.utillService;
import com.kimcompay.projectjb.apis.aws.services.fileService;
import com.kimcompay.projectjb.apis.sns.snsService;
import com.kimcompay.projectjb.enums.senums;
import com.kimcompay.projectjb.users.company.storeService;
import com.kimcompay.projectjb.users.company.tryInsertStoreDto;
import com.kimcompay.projectjb.users.user.tryInsertDto;
import com.kimcompay.projectjb.users.user.tryUpdatePwdDato;
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
    private Logger logger=LoggerFactory.getLogger(restController.class);
    @Autowired
    private snsService snsService;
    @Autowired
    private userService userService;
    @Autowired
    private checkPageService checkPageService;
    @Autowired
    private storeService storeService;
    @Autowired
    private fileService fileService;

    //auth가 앞에있으면 로그인후 이용가능한 api
    @RequestMapping(value = "/auth/test",method = RequestMethod.GET)
    public void name(HttpSession session) {
        logger.info("test");
    }
    @RequestMapping(value = "/auth/user/{action}",method = RequestMethod.GET)
    public JSONObject userAction(@PathVariable String action,HttpServletRequest request) {
        logger.info("userAction controller");
        return userService.selectUserAction(action,request);
    }
    @RequestMapping(value = "/user/{action}",method = RequestMethod.GET)
    public JSONObject userActionNotLogin(@PathVariable String action,HttpServletRequest request) {
        logger.info("userActionNotLogin controller");
        return userService.selectUserAction(action,request);
    }
    @RequestMapping(value = "/sns/**",method = RequestMethod.POST)
    public JSONObject sendSns(@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        logger.info("sendSns Controller");
        return snsService.send(jsonObject, httpSession);
    }
    @RequestMapping(value = "/confrim/{scope}/**",method = RequestMethod.POST)
    public JSONObject checkConfrim(@PathVariable String scope,@RequestBody JSONObject jsonObject,HttpSession httpSession) {
        logger.info("checkConfrim");
        if(scope.equals(senums.auth.get())){
            return snsService.confrim(jsonObject,httpSession);
        }else if(scope.equals("change")){
             return userService.findChangePwdToken(jsonObject.get("val").toString());
        }
        return utillService.getJson(false, "잘못된 요청입니다");
    }
    @RequestMapping(value = "/checkPage/{scope}",method = RequestMethod.GET)
    public JSONObject checkPage(@PathVariable String scope,HttpServletRequest request) {
        logger.info("checkPage controller");
        return checkPageService.checkPage(request, scope);
      
    }
    @RequestMapping(value = "/user/**",method = RequestMethod.POST)
    public JSONObject tryJoin(@Valid @RequestBody tryInsertDto tryInsertDto ,HttpSession session) {
        logger.info("tryJoin");
        logger.info(tryInsertDto.toString());
        return userService.insert(tryInsertDto, session);
    }
    @RequestMapping(value = "/user/{scope}/**",method = RequestMethod.PUT)
    public JSONObject tryChangeUserInfor(@PathVariable String scope,@Valid @RequestBody tryUpdatePwdDato tryUpdatePwdDato) {
        logger.info("tryChangeUserInfor controller");
        return userService.changePwdForLost(scope, tryUpdatePwdDato);
    }
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JSONObject tryLogin(HttpServletRequest request,HttpServletResponse response) {
        logger.info("tryLogin");
        return userService.checkLogin(request, response);
    }
    @RequestMapping(value = "/social/{kind}/{action}",method = RequestMethod.GET)
    public JSONObject soLogin(@PathVariable String kind,@PathVariable String action) {
        logger.info("soLogin");
        return checkPageService.selectPage(kind,action);
    }
    @RequestMapping(value = "/callback/{kind}/{action}",method = RequestMethod.GET)
    public void socialCallback(@PathVariable String kind,@PathVariable String action,HttpServletRequest request) {
        logger.info("socialCallback");
        checkPageService.selectCallback(kind,action,request);
    }
    @RequestMapping("/tokenExpire/{result}")
    public JSONObject tokenExpire(@PathVariable String result,HttpServletRequest request) {
        logger.info("tokenExpire controller");
        if(result.equals(senums.newToken.get())){
            return utillService.getJson(true, "new");
        }else{
            return utillService.getJson(false, "로그인이 만료되었습니다 다시 로그인 바랍니다");
        }
        
    }
    @RequestMapping(value = "/auth/file/{action}",method = RequestMethod.POST)
    public JSONObject imgController(@PathVariable String action,MultipartHttpServletRequest request) {
        logger.info("imgController");
        return fileService.upload(request);
    }
}
