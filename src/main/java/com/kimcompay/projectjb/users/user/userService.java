package com.kimcompay.projectjb.users.user;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import com.kimcompay.projectjb.apis.utillService;
import com.kimcompay.projectjb.apis.jungbu.jungbuService;
import com.kimcompay.projectjb.apis.kakaos.kakaoMapService;
import com.kimcompay.projectjb.configs.securityConfig;
import com.kimcompay.projectjb.enums.senums;
import com.nimbusds.jose.shaded.json.JSONObject;

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
    private kakaoMapService kakaoMapService;
    @Autowired
    private userdao userdao;
    @Autowired
    private securityConfig securityConfig;
    
    public void insert(tryInsertDto tryInsertDto,HttpSession session) {
        logger.info("insert");
        //JSONObject res=jungbuService.getCompanyNum(Integer.parseInt(tryInsertDto.getCompany_num()));
        //휴대폰 문자 인증했는지 검사
        checkAuth(tryInsertDto, session);
        checkValues(tryInsertDto);
        try_insert(tryInsertDto);
    }
    private void try_insert(tryInsertDto tryInsertDto) {
        logger.info("dto_to_vo");
        String hash_pwd=securityConfig.pwdEncoder().encode(tryInsertDto.getPwd());
        int post_code=Integer.parseInt(tryInsertDto.getPost_code());
        if(tryInsertDto.getScope_num()==0){
            logger.info("일반 회원 가입 ");
            userVo vo=userVo.builder().email(tryInsertDto.getEmail()).uaddress(tryInsertDto.getAddress()).udetail_address(tryInsertDto.getDetail_address())
                                        .uphone(tryInsertDto.getPhone()).upostcode(post_code).upwd(hash_pwd)
                                        .usleep(0).build();
        }else{
            logger.info("기업 회원가입");
           /* comVo vo=comVo.builder().cdetail_address(tryInsertDto.getDetail_address()).caddress(tryInsertDto.getAddress()).cemail(tryInsertDto.getEmail()).ckind(tryInsertDto.getScope_num()).cnum(tryInsertDto.getCompany_num())
                                    .cphone(tryInsertDto.getPhone()).cpostcode(post_code).cpwd(hash_pwd).*/
        }
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
            logger.info(entry.getValue().toString());
            if(Integer.parseInt(entry.getValue().toString())!=0){
                throw utillService.makeRuntimeEX("이미 존재하는 "+ senums.valueOf(entry.getKey()).get()+"입니다", "checkValues");
            }
        }
        //비밀번호 일치검사 자리수,공백검사는 validation으로 한다
        String pwd=tryInsertDto.getPwd();
        if(!pwd.equals(tryInsertDto.getPwd2())){
            throw utillService.makeRuntimeEX("비밀번호가 일치 하지 않습니다", "checkValues");
        }
        //주소검사 자리수,공백검사는 validation으로 한다
        JSONObject krespon=kakaoMapService.getAddress(tryInsertDto.getAddress());
        logger.info("주소 조회결과: "+krespon.toString());
        LinkedHashMap<String,Object>meta=(LinkedHashMap<String, Object>)krespon.get("meta");
        if(Integer.parseInt(meta.get("total_count").toString())==0){
            logger.info("주소 검색결과 미존재");
            throw utillService.makeRuntimeEX("주소검색결과가 없습니다", "checkValues");
        }
        tryInsertDto.setScope_num(Integer.parseInt(scope_num));
        logger.info("회원가입 유효성검사 통과");
    }
    private void checkAuth(tryInsertDto tryInsertDto,HttpSession httpSession) {
        logger.info("checkAuth");
        boolean check_email=false;
        boolean check_phone=false;
        String auth_phone=null;
        String auth_email=null;
        try {
            check_phone=Boolean.parseBoolean(httpSession.getAttribute(senums.auth.get()+senums.phonet.get()).toString());
            auth_phone=httpSession.getAttribute(senums.phonet.get()).toString();
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("휴대폰 인증을 먼저 해주세요", "checkAuth");
        }
        try {
            check_email=Boolean.parseBoolean(httpSession.getAttribute(senums.auth.get()+senums.emailt.get()).toString());
            auth_email=httpSession.getAttribute(senums.emailt.get()).toString();
        } catch (Exception e) {
            throw utillService.makeRuntimeEX("이메일 인증을 먼저 해주세요", "checkAuth");
        }
        if(!check_email){
            throw utillService.makeRuntimeEX("이메일 인증이 되지 않았습니다", "checkAuth");
        }else if(!check_phone){
            throw utillService.makeRuntimeEX("휴대폰 인증이 되지 않았습니다", "checkAuth");

        }else if(!tryInsertDto.getPhone().equals(auth_phone)){
            throw utillService.makeRuntimeEX("인증한 휴대폰과 번호가 다릅니다", "checkAuth");
        }else if(!tryInsertDto.getEmail().equals(auth_email)){
            throw utillService.makeRuntimeEX("인증한 이메일과 번호가 다릅니다", "checkAuth");
        }
        logger.info("인증 유효성검사 통과");
    }
}
