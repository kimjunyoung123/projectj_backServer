package com.kimcompay.projectjb.users.user;


import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.apis.utillService;
import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.kimcompay.projectjb.enums.senums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class userService {
    private Logger logger=LoggerFactory.getLogger(userService.class);

    @Autowired
    private jungbuService jungbuService;
    @Autowired
    private userdao userdao;
    
    public void insert(tryInsertDto tryInsertDto,HttpSession session) {
        logger.info("insert");
        //JSONObject res=jungbuService.getCompanyNum(Integer.parseInt(tryInsertDto.getCompany_num()));
        //휴대폰 문자 인증했는지 검사
        //logger.info(res.toString());
        checkAuth(tryInsertDto, session);
        checkValues(tryInsertDto);
    }
    public Map<String,Object> getCount(String val) {
        return userdao.findByPhoneAndEmailJoinCompany(val,val,val,val);
    }
    private void checkValues(tryInsertDto tryInsertDto ) {
        logger.info("checkValues");
        //어떤 유형의 회원인지검사
        String scope=tryInsertDto.getScope();
        String scope_num=null;
        try {
            scope_num=senums.valueOf(scope).get();
        } catch (IllegalArgumentException e) {
            throw utillService.makeRuntimeEX("존재하는 회원 유형이 아닙니다", "checkValues");
        }
        //이메일,전화번호 중복검사 validation 어노테이션으로 null검사는 다하고 온다
        String email=tryInsertDto.getEmail();
        String phone=tryInsertDto.getPhone();
        Map<String,Object>email_phone_count=userdao.findByEmailUsersAndCompanys(email, email);
        email_phone_count=userdao.findByPhoneUsersAndCompanys(phone, phone);
        for(Entry<String, Object> entry:email_phone_count.entrySet()){
            if(!entry.getValue().equals("0")){
                throw utillService.makeRuntimeEX("이미 존재하는 "+ senums.valueOf(entry.getKey())+"입니다", "checkValues");
            }
        }
        //비밀번호 일치검사 자리수,공백검사는 validation으로 한다
        String pwd=tryInsertDto.getPwd();
        if(!pwd.equals(tryInsertDto.getPwd2())){
            throw utillService.makeRuntimeEX("비밀번호가 일치 하지 않습니다", "checkValues");
        }
        
    
    }
    private void checkAuth(tryInsertDto tryInsertDto,HttpSession httpSession) {
        logger.info("checkAuth");
        boolean check_email=false;
        boolean check_phone=false;
        try {
            check_phone=Boolean.parseBoolean(httpSession.getAttribute(senums.auth.get()+senums.phonet.get()).toString());
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("휴대폰 인증을 먼저 해주세요", "checkAuth");
        }
        try {
            check_email=Boolean.parseBoolean(httpSession.getAttribute(senums.auth.get()+senums.emailt.get()).toString());
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("이메일 인증을 먼저 해주세요", "checkAuth");
        }
        if(!check_email){
            throw utillService.makeRuntimeEX("이메일 인증이 되지 않았습니다", "checkAuth");
        }else if(!check_phone){
            throw utillService.makeRuntimeEX("휴대폰 인증이 되지 않았습니다", "checkAuth");

        }
    }
}
